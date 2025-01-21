package fr.vvlabs.notification.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import fr.vvlabs.notification.enums.Evenement;
import fr.vvlabs.notification.enums.FrequenceEvenement;
import fr.vvlabs.notification.record.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    @Value("${spring.kafka.producer.topic-name}")
    private String topicName;
    @Value("${spring.kafka.producer.delay-ms:1000}")
    private int delay;
    @Value("${spring.kafka.producer.max-messages:1000}")
    private int maxMessages;

    public AtomicInteger totalImmediateCount = new AtomicInteger(0);
    public AtomicInteger totalDefferedCount = new AtomicInteger(0);

    public AtomicInteger totalMessageCount = new AtomicInteger(0);

    public AtomicLong startTime = new AtomicLong(0);
    public AtomicLong totalProcessingTime = new AtomicLong(0);
    public ConcurrentHashMap<Integer, Integer> defferedByIdEns = new ConcurrentHashMap<>();

    public void process() throws Exception {

        startTime.set(System.currentTimeMillis());

        int messageCount = 0;
        do {
            int ensId = random.nextInt(10);
            messageCount++;
            Evenement eventType = generateRandomEvent();

            if (FrequenceEvenement.DIFFEREE.equals(eventType.getFrequency())) {
                totalDefferedCount.incrementAndGet();
            } else {
                totalImmediateCount.incrementAndGet();
            }

            Notification notification = Notification
                    .builder()
                    .event(eventType)
                    .ensId(ensId)
                    .params(Map.of("messageCount", "" + messageCount,
                            "ipAddress", "127.0.0.1",
                            "userAgent", "chrome"))
                    .build();

            int nbDefferedForEns = defferedByIdEns.getOrDefault(ensId, 1);
            defferedByIdEns.put(ensId, nbDefferedForEns + 1);

            String authJson = objectMapper.writeValueAsString(notification);
            log.info("sending notification='{}'", authJson);
            kafkaTemplate.send(topicName, authJson);
            updateTime();

            Thread.sleep(delay);

        } while (messageCount < maxMessages);
    }

    private Evenement generateRandomEvent() {
        double randomValue = random.nextDouble() * 100;
        if (randomValue < 36) { // 36% pour AJOUT_VACCINATION
            return Evenement.AJOUT_VACCINATION;
        } else if (randomValue < 93) { // 57% pour AJOUT_DOCUMENT (36 + 57 = 93)
            return Evenement.AJOUT_DOCUMENT;
        } else if (randomValue < 98) { // 5% pour MODIF_DOCUMENT (93 + 5 = 98)
            return Evenement.MODIF_DOCUMENT;
        } else { // 2% restant pour OUVERTURE_ENS
            return Evenement.OUVERTURE_ENS;
        }
    }

    protected void updateTime() {
        totalMessageCount.addAndGet(1);
        // Calculer et accumuler le temps de traitement total après chaque message
        long time = System.currentTimeMillis() - startTime.get();
        totalProcessingTime.set(time);
    }
}
