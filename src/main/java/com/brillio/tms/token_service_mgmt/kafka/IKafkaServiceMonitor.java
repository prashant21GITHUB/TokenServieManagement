package com.brillio.tms.token_service_mgmt.kafka;

/**
 *  Implementation of this interface monitors kafka server's running status.
 *  See {@link KafkaMonitorService}
 */
public interface IKafkaServiceMonitor {
    void startMonitoring(KafkaServiceListener listener);
    void stopMonitoring(KafkaServiceListener listener);
}
