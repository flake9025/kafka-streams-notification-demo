package fr.vvlabs.notification.service.reporting;

import fr.vvlabs.notification.service.consumer.exceptions.NotificationExceptionHandler;
import fr.vvlabs.notification.record.Metrics;
import fr.vvlabs.notification.service.consumer.AbstractNotificationConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final AbstractNotificationConsumer consumer;
    private final NotificationExceptionHandler exceptionHandler;

    public Metrics getMetrics() {
        List<String> windowsOpenContent = new ArrayList<>();
        consumer.windowsListOpen.forEach((key, value) -> {
            windowsOpenContent.add(key + " : " + value);
        });
        List<String> windowsClosedContent = new ArrayList<>();
        consumer.windowsListClosed.forEach((key, value) -> {
            windowsClosedContent.add(key + " : " + value);
        });
        long currentMemoryUsage = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        return new Metrics(
                consumer.totalProcessingTime.get() / 1000,
                consumer.windowsListOpen.size(),
                windowsOpenContent,
                consumer.windowsListClosed.size(),
                windowsClosedContent,
                consumer.totalImmediateCount.get(),
                consumer.totalDefferedCount.get(),
                consumer.totalDefferedGroupedCount.get(),
                consumer.totalMessageCount.get(),
                exceptionHandler.totalErrorsUnknown.get(),
                exceptionHandler.totalErrorsOK.get(),
                exceptionHandler.totalErrorsKO.get(),
                currentMemoryUsage
        );
    }

    @Scheduled(fixedRate = 10000)
    public void logInfos() {
        Metrics metrics = getMetrics();
        String formattedMetrics =
                "-------------------------------------------------------------------------------\n" +
                "                            METRICS INFORMATIONS                               \n" +
                "-------------------------------------------------------------------------------\n" +
                String.format("Total time for %d ms\n", metrics.totalProcessingTime()) +
                String.format("Windows Open: %d, content: [\n\t%s\n]\n", metrics.windowsOpenCount(), String.join("\n\t", metrics.windowsOpenContent())) +
                String.format("Windows Closed: %d, content: [\n\t%s\n]\n", metrics.windowsClosedCount(), String.join("\n\t", metrics.windowsClosedContent())) +
                String.format("Total immediate notifications: %d\n", metrics.totalImmediateNotifications()) +
                String.format("Total deferred notifications: %d\n", metrics.totalDeferredNotifications()) +
                String.format("Total deferred notifications grouped: %d\n", metrics.totalDeferredNotificationsGrouped()) +
                String.format("Total messages (sent to queue): %d\n", metrics.totalMessages()) +
                String.format("Total errors (unknown): %d\n", metrics.totalErrorsUnknown()) +
                String.format("Total errors (sent to DLT): %d\n", metrics.totalErrorsSentToDLT()) +
                String.format("Total errors (not sent to DLT): %d\n", metrics.totalErrorsNotSentToDLT()) +
                "-------------------------------------------------------------------------------\n" +
                String.format("Current memory usage: %d MB\n", metrics.currentMemoryUsage()) +
                "-------------------------------------------------------------------------------\n" +
                "-------------------------------------------------------------------------------\n";
        log.info(formattedMetrics);
    }
}
