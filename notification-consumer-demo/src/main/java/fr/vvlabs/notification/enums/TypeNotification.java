package fr.vvlabs.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TypeNotification {
    N_REG("Notifications réglementaire"),
    N_MES("Notifications messagerie"),
    N_DOC("Notifications documents"),
    N_STORE("Notifications store"),
    N_ENS("Notifications ENS"),
    N_PRV("Notifications prévention"),
    N_DEPIST("Notifications dépistage");

    private final String label;
}
