package fr.vvlabs.notification.service;

import fr.vvlabs.notification.record.Metrics;
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

    private final NotificationProducerService producer;

    public Metrics getMetrics() {
        List<String> windowsOpenContent = new ArrayList<>();
        producer.defferedByIdEns.forEach((key, value) -> {
            windowsOpenContent.add(key + ": " + value);
        });
        long currentMemoryUsage = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        return new Metrics(
                producer.totalProcessingTime.get() / 1000,
                windowsOpenContent,
                producer.totalImmediateCount.get(),
                producer.totalDefferedCount.get(),
                producer.totalMessageCount.get(),
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
                String.format("Windows Open: %d, content: [\n\t%s\n]\n", metrics.windowsOpenContent().size(), String.join("\n\t", metrics.windowsOpenContent())) +
                String.format("Total immediate notifications: %d\n", metrics.totalImmediateNotifications()) +
                String.format("Total deferred notifications: %d\n", metrics.totalDeferredNotifications()) +
                String.format("Total messages : %d\n", metrics.totalMessages()) +
                "-------------------------------------------------------------------------------\n" +
                String.format("Current memory usage: %d MB\n", metrics.currentMemoryUsage()) +
                "-------------------------------------------------------------------------------\n" +
                "-------------------------------------------------------------------------------\n";
        log.info(formattedMetrics);
    }
}
