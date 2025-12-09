package com.dtc.core.cluster.registry;

import com.dtc.core.cluster.registry.ServiceDiscovery;
import com.dtc.core.cluster.registry.ServiceRegistry;

/**
 * 注册中心工厂接口
 * 用于创建 Registry 和 Discovery 实例
 * 
 * @author Network Service Template
 */
public interface RegistryFactory {
    
    /**
     * 创建服务注册器
     * 
     * @param config 注册中心配置
     * @return ServiceRegistry
     */
    ServiceRegistry createRegistry(RegistryConfig config);
    
    /**
     * 创建服务发现器
     * 
     * @param config 注册中心配置
     * @return ServiceDiscovery
     */
    ServiceDiscovery createDiscovery(RegistryConfig config);
    
    /**
     * 支持的类型
     * @return 类型标识
     */
    String supportType();
}

