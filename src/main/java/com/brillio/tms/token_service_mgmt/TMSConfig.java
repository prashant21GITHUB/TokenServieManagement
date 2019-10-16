package com.brillio.tms.token_service_mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * See {@literal application.properties} file under resources directory
 *
 */
@Component
public class TMSConfig {

    //Kafka settings
    @Value(value = "${server.monitor.interval.millis:5000}")
    private long kafkaServerPingInterval;
    @Value(value = "${bootstrap.servers:localhost:9092}")
    private String bootstrapServers;
    @Value(value = "${group.id:brillio}")
    private String groupId;
    @Value(value = "${max.poll.records:100}")
    private int maxPollRecords;
    @Value(value = "${enable.auto.commit:false}")
    private boolean enableAutoCommitFlag;
    @Value(value = "${auto.commit.interval.ms:1000}")
    private long autoCommitIntervalMillis;
    @Value(value = "${session.timeout.ms:30000}")
    private long sessionTimeoutMillis;
    @Value(value = "${heartbeat.interval.ms:10000}")
    private long heartbeatIntervalMillis;

    //Application settings
    @Value(value = "${service.counter.id.category.pairs}")
    private String[] serviceCounterList;
    @Value(value = "${service.counter.queue.names}")
    private String[] serviceCounterQueueNames;
    @Value(value = "${token.generation.counters.size}")
    private int tokenGenerationCountersSize;

    private final Logger LOGGER = LoggerFactory.getLogger("TMSConfig");

    @PostConstruct
    public void logSettings() {
        LOGGER.info("********** Application configurations: START **********");
        LOGGER.info("bootstrap.servers = "+ getBootstrapServers());
        LOGGER.info("group.id = "+ getGroupId());
        LOGGER.info("max.poll.records = "+ getMaxPollRecords());
        LOGGER.info("enable.auto.commit = "+ getEnableAutoCommitFlag());
        LOGGER.info("auto.commit.interval.ms = "+ getAutoCommitIntervalMillis());
        LOGGER.info("session.timeout.ms = "+ getSessionTimeoutMillis());
        LOGGER.info("heartbeat.interval.ms = "+  getHeartbeatIntervalMillis());
        LOGGER.info("server.ping.interval.millis = "+ getKafkaServerPingInterval());
        LOGGER.info("token.generation.counters.size = "+ getTokenGenerationCountersSize());
        LOGGER.info("service.counter.id.category.pairs = "+ Arrays.asList(getServiceCounterList()));
        LOGGER.info("service.counter.queue.names = "+ Arrays.asList(getServiceCounterQueueNames()));
        LOGGER.info("********** Application configurations: END **********");
    }


    public long getKafkaServerPingInterval() {
        return kafkaServerPingInterval;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public boolean getEnableAutoCommitFlag() {
        return enableAutoCommitFlag;
    }

    public long getAutoCommitIntervalMillis() {
        return autoCommitIntervalMillis;
    }

    public long getSessionTimeoutMillis() {
        return sessionTimeoutMillis;
    }

    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    public String[] getServiceCounterList() {
        return serviceCounterList;
    }

    public String[] getServiceCounterQueueNames() {
        return serviceCounterQueueNames;
    }

    public int getTokenGenerationCountersSize() {
        return tokenGenerationCountersSize;
    }

    public long getHeartbeatIntervalMillis() {
        return heartbeatIntervalMillis;
    }
}
