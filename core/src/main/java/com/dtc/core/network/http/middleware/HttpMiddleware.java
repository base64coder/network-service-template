package com.dtc.core.network.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * HTTP 中间件接口
 * 定义 HTTP 中间件的标准接口
 * 
 * @author Network Service Template
 */
public interface HttpMiddleware {

    /**
     * 请求前置处理，在请求到达路由处理器之前执行
     * 
     * @param request HTTP 请求
     * @return 如果返回非null响应则直接返回响应，否则继续处理链
     */
    @Nullable
    default HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        return null;
    }

    /**
     * 请求后置处理，在请求处理完成后执行
     * 
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 处理后的响应，如果返回null则使用原响应
     */
    @Nullable
    default HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        return null;
    }

    /**
     * 获取中间件名称
     */
    @NotNull
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取中间件优先级 数值越小优先级越高
     */
    default int getPriority() {
        return 100;
    }
}
