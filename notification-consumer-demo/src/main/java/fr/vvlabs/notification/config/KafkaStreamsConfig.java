package fr.vvlabs.notification.config;

import fr.vvlabs.notification.service.consumer.exceptions.NotificationExceptionHandler;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id:notification-streams}")
    private String applicationId;

    @Value("${spring.kafka.consumer.notification.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.streams.state-dir:/tmp/kafka-streams}")
    private String stateDir;

    @Value("${spring.kafka.streams.num.stream.threads:4}")
    private int streamThreads;

    @Autowired
    private NotificationExceptionHandler uncaughtExceptionHandler;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Configuration des rejeux
        props.put(CommonClientConfigs.RETRIES_CONFIG, 0);  // Nombre de retries
        props.put(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG, 500);  // Délai entre retries
        props.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 15000);  // Timeout

        // Configuration optimisée pour les performances
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, streamThreads);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 104857600); // 100MB cache
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 20000); // 20 secondes
        props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

        // earliest pour tout relire
        // latest pour lire seulement les nouveaux messsages
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        return new KafkaStreamsConfiguration(props);
    }
}
