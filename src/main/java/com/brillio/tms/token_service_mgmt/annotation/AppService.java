package com.brillio.tms.token_service_mgmt.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *  This annotation is used to provide information about the {@link com.brillio.tms.token_service_mgmt.IAppService} classes to spring
 *  boot framework to call {@link com.brillio.tms.token_service_mgmt.IAppService#start()} on application startup.
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AppService {
}
