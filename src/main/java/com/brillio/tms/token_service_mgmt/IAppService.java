package com.brillio.tms.token_service_mgmt;

/**
 * Its implementations are called by sprint boot application start up to call their start() methods to start
 * the corresponding thread pools. See {@link TokenServiceMgmtApplication}
 *
 * Following are the implementations:
 *  {@link com.brillio.tms.token_service_mgmt.kafka.KafkaMonitorService},
 *  {@link com.brillio.tms.token_service_mgmt.tokenService.ServiceCounterRegistry}
 *
 */
public interface IAppService {
    void start();
    void stop();
}
