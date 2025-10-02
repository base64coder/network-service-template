package com.dtc.core.bootstrap.ioc;

import com.google.inject.AbstractModule;
import com.dtc.api.annotations.NotNull;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.config.ServerInformationImpl;

/**
 * 系统信息模块
 * 绑定系统信息相关的服务
 * 
 * @author Network Service Template
 */
public class SystemInformationModule extends AbstractModule {

    private final @NotNull ServerConfiguration configuration;

    public SystemInformationModule(@NotNull ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        // 绑定服务器配置
        bind(ServerConfiguration.class).toInstance(configuration);

        // 绑定系统信息实现
        bind(com.dtc.api.parameter.ServerInformation.class)
                .to(ServerInformationImpl.class)
                .asEagerSingleton();
    }
}
