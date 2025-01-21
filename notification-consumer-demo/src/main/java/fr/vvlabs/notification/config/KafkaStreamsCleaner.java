package fr.vvlabs.notification.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Slf4j
public class KafkaStreamsCleaner {
    public static void cleanUpState(String appId) {
        log.info("Cleaning up KafkaStreams for app {}", appId);
        Path stateDir = Paths.get("/tmp/kafka-streams", appId);
        try {
            if (Files.exists(stateDir)) {
                Files.walk(stateDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (file.delete()) {
                                log.info("Deleted: " + file.getAbsolutePath());
                            } else {
                                log.warn("Failed to delete: " + file.getAbsolutePath());
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Error cleaning up Kafka Streams state", e);
        }
    }
}
