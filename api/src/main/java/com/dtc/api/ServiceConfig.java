package com.dtc.api;

import com.dtc.api.annotations.NotNull;

/**
 * 服务配置枚举
 * 统一管理所有网络服务的配置信息
 * 
 * @author Network Service Template
 */
public enum ServiceConfig {

    // HTTP 服务配置
    HTTP("HTTP", "http", 8080, 1, 100, "REST API服务，提供HTTP接口"),

    // WebSocket 服务配置
    WEBSOCKET("WebSocket", "websocket", 8081, 2, 90, "WebSocket实时通信服务"),

    // MQTT 服务配置
    MQTT("MQTT", "MQTT", 1883, 3, 80, "MQTT消息队列服务"),

    // TCP 服务配置
    TCP("TCP", "TCP", 9999, 4, 70, "TCP协议服务，支持gRPC"),

    // UDP 服务配置
    UDP("UDP", "UDP", 9997, 5, 65, "UDP协议服务，用于实时通信、物联网、游戏服务器"),

    // Custom 服务配置
    CUSTOM("CustomProtocol", "CustomProtocol", 9998, 6, 60, "自定义协议服务");

    // 服务标识符
    private final String serviceId;

    // 服务名称
    private final String serviceName;

    // 默认端口
    private final int defaultPort;

    // 启动优先级（数字越小优先级越高）
    private final int startupPriority;

    // 服务优先级（数字越大优先级越高）
    private final int servicePriority;

    // 服务描述
    private final String description;

    /**
     * 构造函数
     * 
     * @param serviceId       服务标识符
     * @param serviceName     服务名称
     * @param defaultPort     默认端口
     * @param startupPriority 启动优先级（1-5，数字越小优先级越高）
     * @param servicePriority 服务优先级（60-100，数字越大优先级越高）
     * @param description     服务描述
     */
    ServiceConfig(@NotNull String serviceId,
            @NotNull String serviceName,
            int defaultPort,
            int startupPriority,
            int servicePriority,
            @NotNull String description) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.defaultPort = defaultPort;
        this.startupPriority = startupPriority;
        this.servicePriority = servicePriority;
        this.description = description;
    }

    // ========== Getter 方法 ==========

    @NotNull
    public String getServiceId() {
        return serviceId;
    }

    @NotNull
    public String getServiceName() {
        return serviceName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public int getStartupPriority() {
        return startupPriority;
    }

    public int getServicePriority() {
        return servicePriority;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    // ========== 工具方法 ==========

    /**
     * 根据服务标识符获取配置
     * 
     * @param serviceId 服务标识符
     * @return 服务配置，如果未找到返回null
     */
    public static ServiceConfig getByServiceId(@NotNull String serviceId) {
        for (ServiceConfig config : values()) {
            if (config.getServiceId().equals(serviceId)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 根据端口获取配置
     * 
     * @param port 端口号
     * @return 服务配置，如果未找到返回null
     */
    public static ServiceConfig getByPort(int port) {
        for (ServiceConfig config : values()) {
            if (config.getDefaultPort() == port) {
                return config;
            }
        }
        return null;
    }

    /**
     * 检查端口是否被占用
     * 
     * @param port 端口号
     * @return 如果端口被其他服务占用返回true
     */
    public static boolean isPortOccupied(int port) {
        for (ServiceConfig config : values()) {
            if (config.getDefaultPort() == port && config != getByPort(port)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有服务的启动顺序（按启动优先级排序）
     * 
     * @return 按启动优先级排序的服务配置数组
     */
    @NotNull
    public static ServiceConfig[] getStartupOrder() {
        ServiceConfig[] configs = values();
        java.util.Arrays.sort(configs, (a, b) -> Integer.compare(a.getStartupPriority(), b.getStartupPriority()));
        return configs;
    }

    /**
     * 获取所有服务的优先级顺序（按服务优先级排序）
     * 
     * @return 按服务优先级排序的服务配置数组
     */
    @NotNull
    public static ServiceConfig[] getPriorityOrder() {
        ServiceConfig[] configs = values();
        java.util.Arrays.sort(configs, (a, b) -> Integer.compare(b.getServicePriority(), a.getServicePriority()));
        return configs;
    }

    /**
     * 获取服务配置摘要信息
     * 
     * @return 格式化的服务配置摘要
     */
    @NotNull
    public String getSummary() {
        return String.format("%s (%s) - Port: %d, Startup: %d, Priority: %d - %s",
                serviceId, serviceName, defaultPort, startupPriority, servicePriority, description);
    }

    /**
     * 获取所有服务的配置摘要
     * 
     * @return 所有服务的配置摘要
     */
    @NotNull
    public static String getAllServicesSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== 网络服务配置摘要 ===\n");

        for (ServiceConfig config : getStartupOrder()) {
            summary.append(config.getSummary()).append("\n");
        }

        summary.append("\n=== 启动顺序 ===\n");
        for (int i = 0; i < getStartupOrder().length; i++) {
            ServiceConfig config = getStartupOrder()[i];
            summary.append(String.format("%d. %s (端口: %d)\n", i + 1, config.getServiceName(), config.getDefaultPort()));
        }

        summary.append("\n=== 服务优先级 ===\n");
        for (int i = 0; i < getPriorityOrder().length; i++) {
            ServiceConfig config = getPriorityOrder()[i];
            summary.append(
                    String.format("%d. %s (优先级: %d)\n", i + 1, config.getServiceName(), config.getServicePriority()));
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
