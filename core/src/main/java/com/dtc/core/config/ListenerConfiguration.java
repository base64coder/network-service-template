package com.dtc.core.config;

import com.dtc.api.annotations.NotNull;

import java.util.Objects;

/**
 * 监听器配置 定义网络监听器的配置信息
 * 
 * @author Network Service Template
 */
public class ListenerConfiguration {

    private final @NotNull String type;
    private final int port;
    private final @NotNull String bindAddress;
    private final boolean enabled;
    private final @NotNull String name;
    private final String description;

    public ListenerConfiguration(@NotNull String type, int port, @NotNull String bindAddress, boolean enabled,
            @NotNull String name, String description) {
        this.type = type;
        this.port = port;
        this.bindAddress = bindAddress;
        this.enabled = enabled;
        this.name = name;
        this.description = description;
    }

    @NotNull
    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    @NotNull
    public String getBindAddress() {
        return bindAddress;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ListenerConfiguration that = (ListenerConfiguration) o;
        return port == that.port && enabled == that.enabled && Objects.equals(type, that.type)
                && Objects.equals(bindAddress, that.bindAddress) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, port, bindAddress, enabled, name);
    }

    @Override
    public String toString() {
        return String.format("%s:%d (%s) - %s", type, port, bindAddress, enabled ? "启用" : "禁用");
    }
}
