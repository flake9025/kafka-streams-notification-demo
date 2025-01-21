package fr.vvlabs.notification.web.controller;

import fr.vvlabs.notification.record.Metrics;
import fr.vvlabs.notification.service.reporting.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping
    public Metrics metrics() {
        return reportingService.getMetrics();
    }
}
