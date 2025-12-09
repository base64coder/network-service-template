package com.dtc.core.bootstrap.config;

import com.dtc.api.ServiceConfig;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务配置管理器
 * 统一管理所有网络服务的配置、启动顺序和优先级
 * 
 * @author Network Service Template
 */
public class ServiceConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceConfigManager.class);

    // 服务配置缓存
    private final Map<String, ServiceConfig> serviceConfigs = new ConcurrentHashMap<>();

    // 服务启动状态
    private final Map<String, Boolean> serviceStartupStatus = new ConcurrentHashMap<>();

    // 服务优先级队列
    private final PriorityQueue<ServiceConfig> servicePriorityQueue = new PriorityQueue<>(
            (a, b) -> Integer.compare(b.getServicePriority(), a.getServicePriority()));

    public ServiceConfigManager() {
        initializeServiceConfigs();
        log.info("ServiceConfigManager initialized with {} services", serviceConfigs.size());
    }

    /**
     * 初始化服务配置
     */
    private void initializeServiceConfigs() {
        for (ServiceConfig config : ServiceConfig.values()) {
            serviceConfigs.put(config.getServiceId(), config);
            servicePriorityQueue.offer(config);
            serviceStartupStatus.put(config.getServiceId(), false);
        }

        log.info("Service configurations initialized:");
        for (ServiceConfig config : ServiceConfig.getStartupOrder()) {
            log.info("  - {}: Port {}, Startup Priority {}, Service Priority {}",
                    config.getServiceName(),
                    config.getDefaultPort(),
                    config.getStartupPriority(),
                    config.getServicePriority());
        }
    }

    /**
     * 获取服务配置
     * 
     * @param serviceId 服务标识
     * @return 服务配置，如果未找到返回null
     */
    @Nullable
    public ServiceConfig getServiceConfig(@NotNull String serviceId) {
        return serviceConfigs.get(serviceId);
    }

    /**
     * 获取所有服务配置
     * 
     * @return 所有服务配置的集合
     */
    @NotNull
    public Collection<ServiceConfig> getAllServiceConfigs() {
        return serviceConfigs.values();
    }

    /**
     * 获取按启动优先级排序的服务配置
     * 
     * @return 按启动优先级排序的服务配置列表
     */
    @NotNull
    public List<ServiceConfig> getStartupOrder() {
        return Arrays.asList(ServiceConfig.getStartupOrder());
    }

    /**
     * 获取按服务优先级排序的服务配置
     * 
     * @return 按服务优先级排序的服务配置列表
     */
    @NotNull
    public List<ServiceConfig> getPriorityOrder() {
        return Arrays.asList(ServiceConfig.getPriorityOrder());
    }

    /**
     * 检查端口是否被占用
     * 
     * @param port 端口
     * @return 如果端口被占用返回true
     */
    public boolean isPortOccupied(int port) {
        return ServiceConfig.isPortOccupied(port);
    }

    /**
     * 根据端口获取服务配置
     * 
     * @param port 端口
     * @return 服务配置，如果未找到返回null
     */
    @Nullable
    public ServiceConfig getServiceByPort(int port) {
        return ServiceConfig.getByPort(port);
    }

    /**
     * 标记服务启动状态
     * 
     * @param serviceId 服务标识
     * @param started   是否已启动
     */
    public void setServiceStartupStatus(@NotNull String serviceId, boolean started) {
        serviceStartupStatus.put(serviceId, started);
        log.info("Service {} startup status: {}", serviceId, started ? "STARTED" : "STOPPED");
    }

    /**
     * 检查服务是否已启动
     * 
     * @param serviceId 服务标识
     * @return 如果服务已启动返回true
     */
    public boolean isServiceStarted(@NotNull String serviceId) {
        return serviceStartupStatus.getOrDefault(serviceId, false);
    }

    /**
     * 获取所有已启动的服务
     * 
     * @return 已启动的服务配置列表
     */
    @NotNull
    public List<ServiceConfig> getStartedServices() {
        List<ServiceConfig> startedServices = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : serviceStartupStatus.entrySet()) {
            if (entry.getValue()) {
                ServiceConfig config = serviceConfigs.get(entry.getKey());
                if (config != null) {
                    startedServices.add(config);
                }
            }
        }
        return startedServices;
    }

    /**
     * 获取所有未启动的服务
     * 
     * @return 未启动的服务配置列表
     */
    @NotNull
    public List<ServiceConfig> getStoppedServices() {
        List<ServiceConfig> stoppedServices = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : serviceStartupStatus.entrySet()) {
            if (!entry.getValue()) {
                ServiceConfig config = serviceConfigs.get(entry.getKey());
                if (config != null) {
                    stoppedServices.add(config);
                }
            }
        }
        return stoppedServices;
    }

    /**
     * 获取服务统计信息
     * 
     * @return 服务统计信息
     */
    @NotNull
    public String getServiceStatistics() {
        int totalServices = serviceConfigs.size();
        int startedServices = getStartedServices().size();
        int stoppedServices = getStoppedServices().size();

        StringBuilder stats = new StringBuilder();
        stats.append("=== 服务统计信息 ===\n");
        stats.append(String.format("总服务数: %d\n", totalServices));
        stats.append(String.format("已启动: %d\n", startedServices));
        stats.append(String.format("未启动: %d\n", stoppedServices));
        stats.append(String.format("启动率: %.1f%%\n", (startedServices * 100.0 / totalServices)));

        stats.append("\n=== 启动顺序 ===\n");
        for (int i = 0; i < getStartupOrder().size(); i++) {
            ServiceConfig config = getStartupOrder().get(i);
            String status = isServiceStarted(config.getServiceId()) ? "✓ 已启动" : "✗ 未启动";
            stats.append(String.format("%d. %s (端口: %d) - %s\n",
                    i + 1, config.getServiceName(), config.getDefaultPort(), status));
        }

        stats.append("\n=== 服务优先级 ===\n");
        for (int i = 0; i < getPriorityOrder().size(); i++) {
            ServiceConfig config = getPriorityOrder().get(i);
            String status = isServiceStarted(config.getServiceId()) ? "✓ 已启动" : "✗ 未启动";
            stats.append(String.format("%d. %s (优先级: %d) - %s\n",
                    i + 1, config.getServiceName(), config.getServicePriority(), status));
        }

        return stats.toString();
    }

    /**
     * 获取服务配置摘要
     * 
     * @return 服务配置摘要
     */
    @NotNull
    public String getServiceConfigSummary() {
        return ServiceConfig.getAllServicesSummary();
    }

    /**
     * 重置所有服务状态
     */
    public void resetAllServiceStatus() {
        for (String serviceId : serviceStartupStatus.keySet()) {
            serviceStartupStatus.put(serviceId, false);
        }
        log.info("All service statuses have been reset");
    }

    /**
     * 检查是否有端口冲突
     * 
     * @return 如果有端口冲突返回true
     */
    public boolean hasPortConflicts() {
        Set<Integer> usedPorts = new HashSet<>();
        for (ServiceConfig config : serviceConfigs.values()) {
            int port = config.getDefaultPort();
            if (usedPorts.contains(port)) {
                log.warn("Port conflict detected: Port {} is used by multiple services", port);
                return true;
            }
            usedPorts.add(port);
        }
        return false;
    }

    /**
     * 获取端口冲突信息
     * 
     * @return 端口冲突信息，如果没有冲突返回null
     */
    @Nullable
    public String getPortConflictInfo() {
        Map<Integer, List<String>> portUsage = new HashMap<>();

        for (ServiceConfig config : serviceConfigs.values()) {
            int port = config.getDefaultPort();
            portUsage.computeIfAbsent(port, k -> new ArrayList<>()).add(config.getServiceName());
        }

        StringBuilder conflicts = new StringBuilder();
        boolean hasConflicts = false;

        for (Map.Entry<Integer, List<String>> entry : portUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                hasConflicts = true;
                conflicts.append(String.format("Port %d: %s\n",
                        entry.getKey(), String.join(", ", entry.getValue())));
            }
        }

        return hasConflicts ? conflicts.toString() : null;
    }
}
