package com.dtc.net.cluster.loadbalance;

import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.net.cluster.rpc.proto.RpcRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡策略
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceInstance select(List<ServiceInstance> instances, RpcRequest request) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }
}

