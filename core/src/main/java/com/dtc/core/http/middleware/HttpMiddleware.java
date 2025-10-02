package com.dtc.core.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;

/**
 * HTTP 中间件接口 定义 HTTP 中间件的标准接口
 * 
 * @author Network Service Template
 */
public interface HttpMiddleware {

    /**
     * 请求前处理 在请求到达处理器之前执行
     * 
     * @param request HTTP 请求
     * @return 如果返回非空响应，则直接返回该响应，不继续处理请求
     */
    @Nullable
    default HttpResponse beforeRequest(@NotNull HttpRequest request) {
        return null;
    }

    /**
     * 请求后处理 在请求处理完成后执行
     * 
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 处理后的响应，如果返回 null 则使用原响应
     */
    @Nullable
    default HttpResponse afterRequest(@NotNull HttpRequest request, @NotNull HttpResponse response) {
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
