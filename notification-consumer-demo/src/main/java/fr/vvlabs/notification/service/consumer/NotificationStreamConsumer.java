package fr.vvlabs.notification.service.consumer;

import fr.vvlabs.notification.config.ListStringSerde;
import fr.vvlabs.notification.enums.FrequenceEvenement;
import fr.vvlabs.notification.record.NotificationEvent;
import fr.vvlabs.notification.service.consumer.exceptions.NotificationExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStreamConsumer extends AbstractNotificationConsumer {

    @Value("${spring.kafka.consumer.notification.window-minutes:20}")
    private int windowDurationMinutes;

    @Value("${spring.kafka.consumer.notification.grace-minutes:5}")
    private int graceMinutes;

    @Value("${spring.kafka.consumer.notification.topic}")
    private String notificationInputTopic;

    @Value("${spring.kafka.producer.notification.topic-immediate}")
    private String notificationOutputTopicImmediate;

    @Value("${spring.kafka.producer.notification.topic-deffered}")
    private String notificationOutputTopicDeffered;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public KStream<String, String> notificationPipeline(StreamsBuilder streamsBuilder) {
        initStartTime();


        // Stream principal
        KStream<String, String> stream = streamsBuilder.stream(notificationInputTopic);

        // Transformation de la clé
        KStream<String, String> transformedStream = stream.selectKey((key, value) -> {
            NotificationEvent notificationEvent = parseNotification(value);
            String newKey = notificationEvent.getEnsId() + "|" + notificationEvent.getEvent().getTypeNotification().name();
            log.debug("Transforming key: {} -> {}", key, newKey);
            return newKey;
        });
        stream.peek((key, value) -> log.debug("Stream received event: key={}, value={}", key, value));

        // Séparation en flux différés et immédiats
        Map<String, KStream<String, String>> splitStreams = transformedStream.split(Named.as("notification-"))
                .branch(this::isNotificationDeferred, Branched.as("deferred"))
                .defaultBranch(Branched.as("immediate"));

        KStream<String, String> deferredNotifications = splitStreams.get("notification-deferred");
        KStream<String, String> immediateNotifications = splitStreams.get("notification-immediate");

        // Pour les notifications différées
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(windowDurationMinutes));
        //TimeWindows timeWindows = TimeWindows.ofSizeAndGrace(Duration.ofMinutes(windowDurationMinutes), Duration.ofMinutes(graceMinutes));

        // Agrégation simple
        KTable<Windowed<String>, List<String>> aggregatedNotifications = deferredNotifications
                .groupByKey()
                .windowedBy(timeWindows)
                .aggregate(
                        ArrayList::new,
                        (key, value, aggregate) -> {
                            HashSet<String> newSet = new HashSet<>(aggregate);
                            newSet.add(value);
                            log.debug("Window aggregation key={}, size={}", key, aggregate.size());
                            //LocalDateTime now = LocalDateTime.now();
                            //String startStopDate = now.format(formatter) + "->" +  now.plusMinutes(windowDurationMinutes).format(formatter);
                            log.debug("Window open key={}, size={}", key, aggregate.size());
                            windowsListOpen.put(key, "("+ newSet.size() + ")");
                            return new ArrayList<>(newSet);
                        },
                        Materialized.<String, List<String>, WindowStore<Bytes, byte[]>>as("notification-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new ListStringSerde())
                                // Durée de rétention, sécurité contre les retards à la fermeture
                                .withRetention(Duration.ofMinutes(windowDurationMinutes + graceMinutes * 2L))
                                // Désactive le cache pour assurer la cohérence immédiate entre les pods
                                .withCachingDisabled()
                                .withLoggingDisabled()
                );

        // Traitement des notifications différées agrégées
        aggregatedNotifications
                .suppress(
                        //Suppressed.untilWindowCloses(Suppressed.BufferConfig.maxBytes(1_000_000) // 1MB par fenetre
                        Suppressed.untilWindowCloses(Suppressed.BufferConfig.maxRecords(10)
                        .withNoBound()))
                .toStream()
                .peek((windowedKey, values) -> {
                    log.info("Window closed ! Key: {}, Window Start: {}, Window End: {}, Values Size: {}",
                            windowedKey.key(),
                            windowedKey.window().startTime(),
                            windowedKey.window().endTime(),
                            values.size());

                    windowsListOpen.remove(windowedKey.key());

                    ZonedDateTime startDateTime = ZonedDateTime.ofInstant(windowedKey.window().startTime(), ZoneId.systemDefault());
                    ZonedDateTime endDateTime = ZonedDateTime.ofInstant(windowedKey.window().endTime(), ZoneId.systemDefault());
                    String startDate = startDateTime.format(dateFormatter);
                    String endDate = endDateTime.format(dateFormatter);
                    String startStopDate = startDate + "->" + endDate;

                    windowsListClosed.put(windowedKey.key() + startStopDate, " (" + values.size() + ") ");
                    String notification = values.get(0);
                    String newNotificationEvent = mapDeferredNotification(notification);
                    log.info("processing deferred notification : {}", notification);
                    processNotification(newNotificationEvent);
                    totalDefferedCount.addAndGet(values.size());
                    totalDefferedGroupedCount.incrementAndGet();

                })
                .to(notificationOutputTopicDeffered);

        // Traitement des notifications immédiates
        immediateNotifications
                .peek((windowedKey, value) -> {
                    log.info("processing immediate notification : {}", value);
                    processNotification(value);
                    totalImmediateCount.incrementAndGet();
                })
                .to(notificationOutputTopicImmediate);

        return stream;
    }

    private String mapDeferredNotification(String notification) {
        log.debug("mapping deferred notification : {}", notification);

        NotificationEvent notificationEvent = parseNotification(notification);
        notificationEvent.getParams().put("differee", "true");

        String newNotificationEvent = "";
        try {
            newNotificationEvent = objectMapper.writeValueAsString(notificationEvent);
        }catch (Exception e) {
            log.error("Error while processing deferred notification", e);
        }
        log.debug("mapped deferred notification : {}", newNotificationEvent);
        return newNotificationEvent;
    }

    private boolean isNotificationDeferred(String key, String value) {
        NotificationEvent notificationEvent = parseNotification(value);
        return notificationEvent.getEvent().getFrequency() == FrequenceEvenement.DIFFEREE;
    }
}