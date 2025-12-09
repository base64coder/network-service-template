package com.dtc.core.cluster.registry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务注册接口
 * 定义服务注册的标准行为
 * 
 * @author Network Service Template
 */
public interface ServiceRegistry {
    
    /**
     * 注册服务
     * 
     * @param instance 服务实例
     * @return 异步结果
     */
    CompletableFuture<Void> register(ServiceInstance instance);
    
    /**
     * 注销服务
     * 
     * @param instance 服务实例
     * @return 异步结果
     */
    CompletableFuture<Void> deregister(ServiceInstance instance);
    
    /**
     * 获取注册中心类型
     * @return 类型名称 (e.g., "raft", "nacos")
     */
    String getType();
}

