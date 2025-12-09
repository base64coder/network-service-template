package com.dtc.core.bootstrap.config;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.parameter.ServerInformation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;

/**
 * 服务器信息实现
 * 
 * @author Network Service Template
 */
@Singleton
public class ServerInformationImpl implements ServerInformation {

    private final @NotNull ServerConfiguration configuration;

    @Inject
    public ServerInformationImpl(@NotNull ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    @NotNull
    public String getServerName() {
        return configuration.getServerName();
    }

    @Override
    @NotNull
    public String getServerVersion() {
        return configuration.getServerVersion();
    }

    @Override
    @NotNull
    public String getServerId() {
        return configuration.getServerId();
    }

    @Override
    @NotNull
    public Path getDataFolder() {
        return configuration.getDataFolder();
    }

    @Override
    @NotNull
    public Path getConfigFolder() {
        return configuration.getConfigFolder();
    }

    @Override
    @NotNull
    public Path getExtensionsFolder() {
        return configuration.getExtensionsFolder();
    }

    @Override
    @NotNull
    public Map<String, String> getSystemProperties() {
        return configuration.getSystemProperties();
    }

    @Override
    @NotNull
    public Map<String, String> getEnvironmentVariables() {
        return configuration.getEnvironmentVariables();
    }

    @Override
    public boolean isEmbedded() {
        return configuration.isEmbedded();
    }
}
