package com.dtc.core.bootstrap.ioc;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.bootstrap.config.ConfigurationService;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.bootstrap.config.ServerId;
import com.google.inject.AbstractModule;

/**
 * 配置模块
 * 配置配置服务相关的依赖注入
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
        // 绑定服务器配置实例
        bind(ServerConfiguration.class).toInstance(configuration);

        // 绑定服务器ID实例
        bind(ServerId.class).toInstance(new ServerId(configuration.getServerId()));

        // 绑定配置服务实例
        bind(ConfigurationService.class).asEagerSingleton();
    }
}
