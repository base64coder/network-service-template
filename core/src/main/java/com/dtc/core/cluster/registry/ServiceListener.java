package com.dtc.core.cluster.registry;

import java.util.List;

/**
 * 服务监听器
 * 
 * @author Network Service Template
 */
@FunctionalInterface
public interface ServiceListener {
    
    /**
     * 当服务实例列表发生变化时调用
     * 
     * @param serviceName 服务名称
     * @param instances 最新的实例列表
     */
    void onUpdate(String serviceName, List<ServiceInstance> instances);
}

