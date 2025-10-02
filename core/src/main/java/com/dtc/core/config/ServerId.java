package com.dtc.core.config;

import com.dtc.api.annotations.NotNull;
import java.util.UUID;

/**
 * 服务器ID类
 * 生成和管理服务器唯一标识
 * 
 * @author Network Service Template
 */
public class ServerId {

    private final @NotNull String id;

    public ServerId(@NotNull String id) {
        this.id = id;
    }

    public ServerId() {
        this.id = "network-service-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @NotNull
    public String get() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
