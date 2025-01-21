package fr.vvlabs.notification.service.notification;

import fr.vvlabs.notification.record.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SmirService smirService;

    public void buildNotification(NotificationEvent notificationEvent) {
        log.info("Début de traitement Eip ENS notification : {}", notificationEvent);

        switch (notificationEvent.getEvent()){
            case AJOUT_DOCUMENT :
                log.info("Ajout de document ...");
                // exception IOException aléatoire 2% du temps
                if (Math.random() < 0.02) { // 1% de chances
                    log.error("Erreur IOException pendant l'ajout de document.");
                    throw new RuntimeException("Erreur lors de l'ajout du document.");
                }
                log.info("Ajout de document ... OK");
                break;
            case MODIF_DOCUMENT :
                log.info("Modification de document ... ");
                // exception IOException aléatoire 2% du temps
                if (Math.random() < 0.02) { // 1% de chances
                    log.error("Erreur IOException pendant la modification de document.");
                    throw new RuntimeException("Erreur lors de la modification du document.");
                }
                log.info("Modification de document ... OK");
                break;
            case AJOUT_VACCINATION:
                log.info("Ajout Entourage ... appel API SMIR");
                smirService.getSmirCoordonnees(notificationEvent.getEnsId());
                log.info("Ajout Entourage ... OK");
                break;
            case OUVERTURE_ENS :
                log.info("Ouverture ENS ... appel API SMIR");
                smirService.getSmirCoordonnees(notificationEvent.getEnsId());
                log.info("Ouverture ENS ... OK");
                break;
            case INCITATION_ENROLEMENT :
                log.info("Incitation Enrolement : appel API SMIR");
                smirService.getSmirCoordonnees(notificationEvent.getEnsId());
                log.info("Incitation Enrolement ... OK");
                break;
        }
        log.info("Fin de traitement Eip ENS notification : {}", notificationEvent);
    }
}
