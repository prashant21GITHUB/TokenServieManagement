package com.brillio.tms.token_service_mgmt.kafka;

import com.brillio.tms.token_service_mgmt.TMSConfig;
import com.brillio.tms.token_service_mgmt.models.ApplicantTokenRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide kafkaTemplate instance to publish message on kafka topic.
 */
@Configuration
public class KafkaProducerConfig {

    @Autowired
    private TMSConfig config;

    @Bean
    public ProducerFactory<String, ApplicantTokenRecord> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getBootstrapServers());
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "com.brillio.tms.token_service_mgmt.kafka.json.ObjectToJsonSerializer");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ApplicantTokenRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
