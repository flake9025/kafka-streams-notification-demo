package fr.vvlabs.notification.service.consumer.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.vvlabs.notification.exception.NotificationEventException;
import fr.vvlabs.notification.record.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class NotificationExceptionHandler {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public AtomicInteger totalErrorsUnknown = new AtomicInteger(0);
    public AtomicInteger totalErrorsOK = new AtomicInteger(0);
    public AtomicInteger totalErrorsKO = new AtomicInteger(0);

    @Value("${spring.kafka.consumer.notification.dlt}")
    private String deadLetterTopic;

    @Deprecated(since = "use handleException with notificationEvent")
    public void handleException(Throwable exception) {
        log.info("NotificationExceptionHandler : handle {}", exception.getClass().getSimpleName());
        // On récupère le message d'origine si possible via la cause de l'exception
        NotificationEvent notificationEvent = extractNotificationEvent(exception);
        handleException(exception, notificationEvent);
    }
    public void handleException(Throwable exception, NotificationEvent notificationEvent) {
        log.info("NotificationExceptionHandler : exception {}, notification {}", exception.getClass().getSimpleName(), notificationEvent);

        if (notificationEvent == null) {
            log.warn("NotificationEvent is null for exception {}, skipping !", exception.getMessage(), exception);
            totalErrorsUnknown.incrementAndGet();
        } else {
            // Envoi du message erroné au DLT si on a pu le récupérer
            try {
                kafkaTemplate.send(deadLetterTopic, objectMapper.writeValueAsString(notificationEvent));
                log.info("Message sent to DLT: {}", notificationEvent);
                totalErrorsOK.incrementAndGet();
            } catch (Exception e) {
                log.error("Failed to send message to DLT: {}", notificationEvent, e);
                totalErrorsKO.incrementAndGet();
            }
        }
    }

    private NotificationEvent extractNotificationEvent(Throwable exception) {
        // Parcours recursif des causes de l'exception pour trouver le message original
        if (exception == null) {
            return null;
        }

        log.info("NotificationExceptionHandler : extractNotificationEvent {}", exception.getClass().getSimpleName());

        // Si l'exception contient directement le message
        if (exception instanceof NotificationEventException) {
            return ((NotificationEventException) exception).getNotificationEvent();
        }

        // Recherche récursive dans la cause
        return extractNotificationEvent(exception.getCause());
    }
}
