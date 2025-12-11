package com.dtc.net.cluster.rpc;

import com.dtc.api.rpc.RpcService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储本地暴露的 RPC 服务信息
 */
public class RpcProviderRegistry {
    
    private final Map<String, Object> serviceBeans = new ConcurrentHashMap<>();
    private final Map<String, RpcService> serviceMetadata = new ConcurrentHashMap<>();
    
    public void addService(String serviceName, Object bean, RpcService metadata) {
        serviceBeans.put(serviceName, bean);
        serviceMetadata.put(serviceName, metadata);
    }
    
    public Map<String, Object> getServiceBeans() {
        return serviceBeans;
    }
    
    public RpcService getMetadata(String serviceName) {
        return serviceMetadata.get(serviceName);
    }
}

