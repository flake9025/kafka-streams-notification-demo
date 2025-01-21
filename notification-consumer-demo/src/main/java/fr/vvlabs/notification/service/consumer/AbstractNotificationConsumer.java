package fr.vvlabs.notification.service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.vvlabs.notification.exception.NotificationEventException;
import fr.vvlabs.notification.record.NotificationEvent;
import fr.vvlabs.notification.service.consumer.exceptions.NotificationExceptionHandler;
import fr.vvlabs.notification.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractNotificationConsumer {

    @Autowired
    protected NotificationService notificationService;
    @Autowired
    protected NotificationExceptionHandler notificationExceptionHandler;
    @Autowired
    protected ObjectMapper objectMapper;

    public AtomicLong startTime = new AtomicLong(0);

    public ConcurrentHashMap<String, String> windowsListOpen = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> windowsListClosed = new ConcurrentHashMap<>();
    public AtomicInteger totalImmediateCount = new AtomicInteger(0);
    public AtomicInteger totalDefferedCount = new AtomicInteger(0);

    public AtomicInteger totalDefferedGroupedCount = new AtomicInteger(0);
    public AtomicInteger totalMessageCount = new AtomicInteger(0);

    public AtomicLong totalProcessingTime = new AtomicLong(0);

    protected NotificationEvent parseNotification(String notificationEns) {
        if (notificationEns == null || notificationEns.isBlank()) {
            log.warn("Empty or null notification received");
            return null;
        }

        try {
            return objectMapper.readValue(notificationEns, NotificationEvent.class);
        } catch (Exception e) {
            log.error("Error parsing notification: {}, {}", notificationEns, e.getMessage());
            notificationExceptionHandler.handleException(e);
            return null;
        }
    }

    protected void processNotification(String notificationEns) {
        NotificationEvent notificationEvent = parseNotification(notificationEns);

        if (notificationEvent == null) {
            log.warn("Skipping null notificationEvent");
            return; // Ne rien faire si la désérialisation a échoué
        }

        try {
            notificationService.buildNotification(notificationEvent);
            updateTime();
        } catch (Exception e) {
            log.error("processNotification KO : {}", e.getMessage());
            notificationExceptionHandler.handleException(e, notificationEvent);
        }
    }

    protected void initStartTime() {
        if (totalMessageCount.get() == 0) {
            startTime.set(System.currentTimeMillis());
        }
    }

    protected void updateTime() {
        totalMessageCount.addAndGet(1);
        // Calculer et accumuler le temps de traitement total après chaque message
        long time = System.currentTimeMillis() - startTime.get();
        totalProcessingTime.set(time);
    }
}

