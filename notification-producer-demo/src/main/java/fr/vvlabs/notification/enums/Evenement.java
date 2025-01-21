package fr.vvlabs.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Evenement {
    AJOUT_DOCUMENT(TypeNotification.N_DOC, FrequenceEvenement.DIFFEREE),
    MODIF_DOCUMENT(TypeNotification.N_DOC, FrequenceEvenement.DIFFEREE),
    AJOUT_VACCINATION(TypeNotification.N_DOC, FrequenceEvenement.DIFFEREE),
    OUVERTURE_ENS(TypeNotification.N_REG, null),
    INCITATION_ENROLEMENT(TypeNotification.N_ENS, null);

    private final TypeNotification typeNotification;
    private final FrequenceEvenement frequency;
}
