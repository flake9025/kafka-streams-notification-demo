package fr.vvlabs.notification.record;

import fr.vvlabs.notification.enums.Evenement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private long ensId;
    private Evenement event;
    private Map<String, String> params = new HashMap<>();
}
