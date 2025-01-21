package fr.vvlabs.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducerTask {

    private final NotificationProducerService notificationProducerService;

    @Async
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() throws Exception {
        notificationProducerService.process();
    }
}
