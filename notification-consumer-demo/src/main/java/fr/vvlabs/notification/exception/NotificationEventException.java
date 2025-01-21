package fr.vvlabs.notification.exception;

import fr.vvlabs.notification.record.NotificationEvent;
import lombok.Getter;

public class NotificationEventException extends RuntimeException {

    @Getter
    private NotificationEvent notificationEvent;

    public NotificationEventException(String message, NotificationEvent event, Throwable cause) {
        super(message, cause);
        this.notificationEvent = event;
    }
}
