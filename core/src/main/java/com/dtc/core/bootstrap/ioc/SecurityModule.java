package com.dtc.core.bootstrap.ioc;

import com.dtc.core.security.AuthenticationService;
import com.dtc.core.security.AuthorizationService;
import com.dtc.core.security.SecurityManager;
import com.google.inject.AbstractModule;

/**
 * 安全模块
 * 绑定安全相关的服务
 * 
 * @author Network Service Template
 */
public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定安全管理器
        bind(SecurityManager.class).asEagerSingleton();

        // 绑定认证服务
        bind(AuthenticationService.class).asEagerSingleton();

        // 绑定授权服务
        bind(AuthorizationService.class).asEagerSingleton();
    }
}
