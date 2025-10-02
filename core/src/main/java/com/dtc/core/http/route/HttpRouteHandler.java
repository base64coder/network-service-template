package com.dtc.core.http.route;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;

/**
 * HTTP 路由处理器接口 定义处理 HTTP 请求的方法
 * 
 * @author Network Service Template
 */
@FunctionalInterface
public interface HttpRouteHandler {

    /**
     * 处理 HTTP 请求
     * 
     * @param request HTTP 请求
     * @return HTTP 响应
     */
    @NotNull
    HttpResponse handle(@NotNull HttpRequest request);
}
