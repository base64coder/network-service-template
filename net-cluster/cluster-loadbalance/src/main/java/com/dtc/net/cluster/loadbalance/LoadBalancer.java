package com.dtc.net.cluster.loadbalance;

import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.net.cluster.rpc.proto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略接口
 */
public interface LoadBalancer {
    
    /**
     * 选择服务实例
     *
     * @param instances 可用的服务实例列表
     * @param request 当前的RPC请求（可用于一致性哈希等策略）
     * @return 选择的服务实例，如果没有可用实例则返回 null
     */
    ServiceInstance select(List<ServiceInstance> instances, RpcRequest request);
}

