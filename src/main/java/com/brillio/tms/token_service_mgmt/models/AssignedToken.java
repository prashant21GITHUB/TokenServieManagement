package com.brillio.tms.token_service_mgmt.models;


import com.brillio.tms.token_service_mgmt.tokenService.IServiceCounter;

public class AssignedToken {

    private final Token token;
    private final IServiceCounter serviceCounter;

    public AssignedToken(Token token, IServiceCounter serviceCounter) {
        this.token = token;
        this.serviceCounter = serviceCounter;
    }

    public Token getToken() {
        return token;
    }

    public IServiceCounter getServiceCounter() {
        return serviceCounter;
    }
}
