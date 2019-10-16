package com.brillio.tms.token_service_mgmt.kafka;

/**
 * Callback listener provided by each subscriber to {@link KafkaMonitorService}
 */
public interface KafkaServiceListener {
    void onRunningStatusChanged(boolean isRunning);
}
