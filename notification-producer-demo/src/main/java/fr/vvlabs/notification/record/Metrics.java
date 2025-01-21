package fr.vvlabs.notification.record;

import java.util.List;

public record Metrics(
        long totalProcessingTime,
        List<String> windowsOpenContent,
        int totalImmediateNotifications,
        int totalDeferredNotifications,
        int totalMessages,
        long currentMemoryUsage
) {}
