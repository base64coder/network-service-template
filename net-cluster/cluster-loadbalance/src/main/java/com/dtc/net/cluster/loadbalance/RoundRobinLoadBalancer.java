package com.dtc.net.cluster.loadbalance;

import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.net.cluster.rpc.proto.RpcRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public ServiceInstance select(List<ServiceInstance> instances, RpcRequest request) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        int index = Math.abs(counter.getAndIncrement() % instances.size());
        return instances.get(index);
    }
}

