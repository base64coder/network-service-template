package com.dtc.core.bootstrap.config;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置服务
 * 管理服务器配置
 * 
 * @author Network Service Template
 */
@Singleton
public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final @NotNull ServerConfiguration serverConfiguration;
    private final @NotNull Map<String, String> dynamicConfiguration;

    @Inject
    public ConfigurationService(@NotNull ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.dynamicConfiguration = new ConcurrentHashMap<>();

        log.info("Configuration service initialized for server: {}", serverConfiguration.getServerName());
    }

    @NotNull
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Nullable
    public String getConfiguration(@NotNull String key) {
        return dynamicConfiguration.get(key);
    }

    @Nullable
    public String getConfiguration(@NotNull String key, @Nullable String defaultValue) {
        return dynamicConfiguration.getOrDefault(key, defaultValue);
    }

    public void setConfiguration(@NotNull String key, @NotNull String value) {
        dynamicConfiguration.put(key, value);
        log.debug("Configuration updated: {} = {}", key, value);
    }

    public void removeConfiguration(@NotNull String key) {
        dynamicConfiguration.remove(key);
        log.debug("Configuration removed: {}", key);
    }

    @NotNull
    public Map<String, String> getAllConfiguration() {
        return Map.copyOf(dynamicConfiguration);
    }
}
