package com.dtc.net.cluster.rpc.router;

import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.net.cluster.rpc.context.RpcContext;
import com.dtc.net.cluster.rpc.proto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签路由实现
 * 优先匹配带有相同 tag 的服务实例
 */
public class TagRouter implements ServiceRouter {

    private static final Logger log = LoggerFactory.getLogger(TagRouter.class);
    public static final String TAG_KEY = "tag";

    @Override
    public List<ServiceInstance> route(List<ServiceInstance> instances, RpcRequest request) {
        // 从 RpcContext 或 Request Attachments 中获取 tag
        String targetTag = request.getAttachmentsMap().get(TAG_KEY);
        if (targetTag == null) {
            targetTag = RpcContext.getContext().getAttachment(TAG_KEY);
        }

        if (targetTag == null || targetTag.isEmpty()) {
            // 如果没有指定 tag，优先调用没有 tag 的实例，或者返回所有（视策略而定）
            // 这里简单返回所有，由负载均衡决定
            return instances;
        }

        String finalTargetTag = targetTag;
        List<ServiceInstance> taggedInstances = instances.stream()
                .filter(instance -> finalTargetTag.equals(instance.getMetadata().get(TAG_KEY)))
                .collect(Collectors.toList());

        if (!taggedInstances.isEmpty()) {
            return taggedInstances;
        }

        // 降级策略：如果找不到指定 tag 的实例，是否允许调用其他实例？
        // 这里默认不允许（强路由），如果需要弱路由可以返回 instances
        log.warn("No service instances found for tag: {}, returning empty list", targetTag);
        return taggedInstances; 
    }
}

