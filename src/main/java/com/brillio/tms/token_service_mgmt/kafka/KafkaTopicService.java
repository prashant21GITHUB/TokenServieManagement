package com.brillio.tms.token_service_mgmt.kafka;

import com.brillio.tms.token_service_mgmt.TMSConfig;
import com.brillio.tms.token_service_mgmt.tokenService.ServiceCounterRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *  Used to create Kafka topic, See {@link ServiceCounterRegistry#start()}
 */
@Service
public class KafkaTopicService {

    private final Properties kafkaProperties;
    private final TMSConfig config;
    private final Logger LOGGER = LoggerFactory.getLogger("KafkaTopicService");

    @Autowired
    public KafkaTopicService(TMSConfig config) {
        this.config = config;
        kafkaProperties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        kafkaProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
    }

    public void createTopic(String topicName) {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(kafkaProperties);
            NewTopic newTopic = new NewTopic(topicName, 1, (short)1); //new NewTopic(topicName, numPartitions, replicationFactor)
            LOGGER.info("Creat kafka topic: "+ topicName);
            adminClient.createTopics(Collections.singletonList(newTopic));
        } catch (Exception e) {
            LOGGER.error("Failed to create topic: "+topicName+ ", Error: " + e.getMessage());
        } finally {
            if(adminClient != null) {
                adminClient.close();
            }
        }
    }

    public boolean isKafkaServerRunning() {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(kafkaProperties);
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> names = topics.names().get(2000, TimeUnit.MILLISECONDS);
            if (names.isEmpty()) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            if(adminClient != null) {
                adminClient.close(2000, TimeUnit.MILLISECONDS);
            }
        }

    }
}
