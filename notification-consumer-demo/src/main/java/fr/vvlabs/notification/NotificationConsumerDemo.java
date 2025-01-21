package fr.vvlabs.notification;

import fr.vvlabs.notification.config.KafkaStreamsCleaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableScheduling
public class NotificationConsumerDemo {

    public static void main(String[] args) {

        String appId = "ens-notification-consumer-demo";
        boolean cleanOnStartup = System.getProperty("cleanOnStartup", "false").equals("true");
        log.info("clean On Startup : {}", cleanOnStartup);
        if(cleanOnStartup) {
            KafkaStreamsCleaner.cleanUpState(appId);
        }

        // Démarrage de l'application et récupération du contexte Spring
        ConfigurableApplicationContext context = SpringApplication.run(NotificationConsumerDemo.class, args);

        // Récupération de l'environnement pour accéder aux propriétés
        Environment env = context.getEnvironment();

        // Récupération de la propriété server.url
        String serverUrl = env.getProperty("server.url");

        // Construction de l'URL Swagger et affichage
        System.out.println("----------------------------------------------------------------");
        System.out.println("Swagger UI available at: " + serverUrl + "/swagger-ui/index.html");
    }
}
