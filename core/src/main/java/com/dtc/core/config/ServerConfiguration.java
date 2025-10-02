package com.dtc.core.config;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器配置类
 * 
 * @author Network Service Template
 */
public class ServerConfiguration {

    private final @NotNull String serverName;
    private final @NotNull String serverVersion;
    private final @NotNull String serverId;
    private final @NotNull Path dataFolder;
    private final @NotNull Path configFolder;
    private final @NotNull Path extensionsFolder;
    private final @NotNull Map<String, String> systemProperties;
    private final @NotNull Map<String, String> environmentVariables;
    private final boolean embedded;

    private ServerConfiguration(Builder builder) {
        this.serverName = builder.serverName;
        this.serverVersion = builder.serverVersion;
        this.serverId = builder.serverId;
        this.dataFolder = builder.dataFolder;
        this.configFolder = builder.configFolder;
        this.extensionsFolder = builder.extensionsFolder;
        this.systemProperties = new HashMap<>(builder.systemProperties);
        this.environmentVariables = new HashMap<>(builder.environmentVariables);
        this.embedded = builder.embedded;
    }

    @NotNull
    public String getServerName() {
        return serverName;
    }

    @NotNull
    public String getServerVersion() {
        return serverVersion;
    }

    @NotNull
    public String getServerId() {
        return serverId;
    }

    @NotNull
    public Path getDataFolder() {
        return dataFolder;
    }

    @NotNull
    public Path getConfigFolder() {
        return configFolder;
    }

    @NotNull
    public Path getExtensionsFolder() {
        return extensionsFolder;
    }

    @NotNull
    public Map<String, String> getSystemProperties() {
        return new HashMap<>(systemProperties);
    }

    @NotNull
    public Map<String, String> getEnvironmentVariables() {
        return new HashMap<>(environmentVariables);
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String serverName = "Network Service";
        private String serverVersion = "1.0.0";
        private String serverId = "network-service-" + System.currentTimeMillis();
        private Path dataFolder = Paths.get("data");
        private Path configFolder = Paths.get("conf");
        private Path extensionsFolder = Paths.get("extensions");
        private Map<String, String> systemProperties = new HashMap<>();
        private Map<String, String> environmentVariables = new HashMap<>();
        private boolean embedded = false;

        public Builder serverName(@NotNull String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder serverVersion(@NotNull String serverVersion) {
            this.serverVersion = serverVersion;
            return this;
        }

        public Builder serverId(@NotNull String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder dataFolder(@NotNull String dataFolder) {
            this.dataFolder = Paths.get(dataFolder);
            return this;
        }

        public Builder configFolder(@NotNull String configFolder) {
            this.configFolder = Paths.get(configFolder);
            return this;
        }

        public Builder extensionsFolder(@NotNull String extensionsFolder) {
            this.extensionsFolder = Paths.get(extensionsFolder);
            return this;
        }

        public Builder systemProperty(@NotNull String key, @NotNull String value) {
            this.systemProperties.put(key, value);
            return this;
        }

        public Builder environmentVariable(@NotNull String key, @NotNull String value) {
            this.environmentVariables.put(key, value);
            return this;
        }

        public Builder embedded(boolean embedded) {
            this.embedded = embedded;
            return this;
        }

        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }
    }
}
