package com.dtc.core.security;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 安全管理器
 * 管理安全相关的功能
 * 
 * @author Network Service Template
 */
@Singleton
public class SecurityManager {

    private static final Logger log = LoggerFactory.getLogger(SecurityManager.class);

    private final @NotNull AuthenticationService authenticationService;
    private final @NotNull AuthorizationService authorizationService;

    @Inject
    public SecurityManager(
            @NotNull AuthenticationService authenticationService,
            @NotNull AuthorizationService authorizationService) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;

        log.info("Security manager initialized");
    }

    /**
     * 获取认证服务
     * 
     * @return 认证服务
     */
    @NotNull
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * 获取授权服务
     * 
     * @return 授权服务
     */
    @NotNull
    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }
}
