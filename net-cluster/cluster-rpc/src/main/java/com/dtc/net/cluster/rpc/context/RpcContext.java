package com.dtc.net.cluster.rpc.context;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 上下文，用于在调用链中传递参数（如路由标签、链路追踪ID等）
 */
public class RpcContext {

    private static final ThreadLocal<RpcContext> LOCAL = ThreadLocal.withInitial(RpcContext::new);

    private final Map<String, String> attachments = new HashMap<>();

    public static RpcContext getContext() {
        return LOCAL.get();
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    public RpcContext setAttachment(String key, String value) {
        attachments.put(key, value);
        return this;
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }
}

