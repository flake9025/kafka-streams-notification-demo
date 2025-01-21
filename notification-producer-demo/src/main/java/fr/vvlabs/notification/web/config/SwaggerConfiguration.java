package fr.vvlabs.notification.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Value("${server.url:http://localhost:8080}")
    private String serverAddress;

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        String serverUrl = serverAddress;
        Server server = new Server().url(serverUrl);
        return new OpenAPI()
                .info(new Info().title("Kafka Streams Demo Producer API")
                        .description("API Documentation")
                        .version("1.0")
                )
                .servers(List.of(server));
    }
}
