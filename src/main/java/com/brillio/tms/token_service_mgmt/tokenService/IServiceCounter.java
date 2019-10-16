package com.brillio.tms.token_service_mgmt.tokenService;

import com.brillio.tms.token_service_mgmt.enums.TokenCategory;

/**
 *  Each service counter must subscribe to a kafka topic(Queue name).
 *  Implementation: {@link ServiceCounter}
 *
 */
public interface IServiceCounter {
    String getName();
    String getQueueName();

    TokenCategory servingTokenCategory();
}
