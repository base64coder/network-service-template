package com.dtc.core.cluster.registry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务发现接口
 * 
 * @author Network Service Template
 */
public interface ServiceDiscovery {
    
    /**
     * 获取服务的所有实例
     * 
     * @param serviceName 服务名称
     * @return 实例列表
     */
    CompletableFuture<List<ServiceInstance>> getInstances(String serviceName);
    
    /**
     * 获取所有服务名称
     * 
     * @return 服务名称列表
     */
    CompletableFuture<List<String>> getServices();
    
    /**
     * 订阅服务变化
     * 
     * @param serviceName 服务名称
     * @param listener 监听器
     */
    void subscribe(String serviceName, ServiceListener listener);
    
    /**
     * 取消订阅
     * 
     * @param serviceName 服务名称
     * @param listener 监听器
     */
    void unsubscribe(String serviceName, ServiceListener listener);
}

