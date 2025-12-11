package com.dtc.net.cluster.rpc.router;

import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.net.cluster.rpc.proto.RpcRequest;

import java.util.List;

/**
 * 服务路由接口
 * 用于在负载均衡前筛选服务实例
 */
public interface ServiceRouter {

    /**
     * 路由筛选
     *
     * @param instances 原始服务实例列表
     * @param request RPC请求
     * @return 筛选后的服务实例列表
     */
    List<ServiceInstance> route(List<ServiceInstance> instances, RpcRequest request);
}

