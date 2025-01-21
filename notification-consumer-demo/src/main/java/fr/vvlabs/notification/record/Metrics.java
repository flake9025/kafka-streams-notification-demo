package fr.vvlabs.notification.record;

import java.util.List;

public record Metrics(
        long totalProcessingTime,
        int windowsOpenCount,
        List<String> windowsOpenContent,
        int windowsClosedCount,
        List<String> windowsClosedContent,
        int totalImmediateNotifications,
        int totalDeferredNotifications,
        int totalDeferredNotificationsGrouped,
        int totalMessages,
        int totalErrorsUnknown,
        int totalErrorsSentToDLT,
        int totalErrorsNotSentToDLT,
        long currentMemoryUsage
) {}
