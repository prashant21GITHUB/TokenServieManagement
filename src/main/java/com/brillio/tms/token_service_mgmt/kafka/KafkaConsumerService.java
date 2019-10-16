package com.brillio.tms.token_service_mgmt.kafka;

import com.brillio.tms.token_service_mgmt.TMSConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * The users of this class can create a KafkaConsumer and subscribe to a kafka topic with help of KafkaConsumer API.
 * See {@link KafkaConsumerService#newConsumer()}
 *
 * Also see {@link com.brillio.tms.token_service_mgmt.tokenService.ServiceCounter)}
 */
@Service
public class KafkaConsumerService {

    private final Properties kafkaProperties = new Properties();
    private final TMSConfig config;

    @Autowired
    public KafkaConsumerService(TMSConfig config) {
        this.config = config;
        this.loadProperties();
    }

    public <K,V> Consumer<K,V> newConsumer() {
        Consumer<K, V> consumer =  new KafkaConsumer<>(kafkaProperties);
        return consumer;
    }

    private void loadProperties() {
        kafkaProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        kafkaProperties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(config.getEnableAutoCommitFlag()));
        kafkaProperties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(config.getAutoCommitIntervalMillis()));
        kafkaProperties.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, String.valueOf(config.getHeartbeatIntervalMillis()));
        kafkaProperties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(config.getSessionTimeoutMillis()));
        kafkaProperties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(config.getMaxPollRecords()));
//        kafkaProperties.setProperty(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        kafkaProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.brillio.tms.models.ApplicantTokenRecord");
//        kafkaProperties.setProperty(JsonDeserializer.TRUSTED_PACKAGES, "com.brillio.tms.models.*");
        kafkaProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        kafkaProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.brillio.tms.token_service_mgmt.kafka.json.JsonToObjectDeserializer");
    }
}
