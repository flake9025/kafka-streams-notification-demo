package fr.vvlabs.notification.record;

import fr.vvlabs.notification.enums.Evenement;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Notification {

    private long ensId;
    private Evenement event;
    private Map<String, String> params;
}
