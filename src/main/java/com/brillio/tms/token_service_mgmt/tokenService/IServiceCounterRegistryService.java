package com.brillio.tms.token_service_mgmt.tokenService;

import com.brillio.tms.token_service_mgmt.models.Token;

/**
 * Creates and maintains the list of {@link ServiceCounter}
 * Implementation: {@link ServiceCounterRegistry}
 */
public interface IServiceCounterRegistryService {
    IServiceCounter getServiceCounterForToken(Token token);
}
