package com.dtc.core.bootstrap.ioc;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.config.ConfigurationService;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.config.ServerId;
import com.google.inject.AbstractModule;

/**
 * 配置模块
 * 绑定配置相关的服务
 * 
 * @author Network Service Template
 */
public class ConfigurationModule extends AbstractModule {

    private final @NotNull ServerConfiguration configuration;

    public ConfigurationModule(@NotNull ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        // 绑定服务器配置
        bind(ServerConfiguration.class).toInstance(configuration);

        // 绑定服务器ID
        bind(ServerId.class).toInstance(new ServerId(configuration.getServerId()));

        // 绑定配置服务
        bind(ConfigurationService.class).asEagerSingleton();
    }
}
