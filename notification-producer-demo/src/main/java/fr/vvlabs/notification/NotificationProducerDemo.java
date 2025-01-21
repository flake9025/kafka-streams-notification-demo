package fr.vvlabs.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotificationProducerDemo {

	public static void main(String[] args) {

		// Démarrage de l'application et récupération du contexte Spring
		ConfigurableApplicationContext context = SpringApplication.run(NotificationProducerDemo.class, args);

		// Récupération de l'environnement pour accéder aux propriétés
		Environment env = context.getEnvironment();

		// Récupération de la propriété server.url
		String serverUrl = env.getProperty("server.url");

		// Construction de l'URL Swagger et affichage
		System.out.println("----------------------------------------------------------------");
		System.out.println("Swagger UI available at: " + serverUrl + "/swagger-ui/index.html");
	}
}
