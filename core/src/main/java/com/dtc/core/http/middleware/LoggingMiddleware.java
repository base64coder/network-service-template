package com.dtc.core.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志中间件 记录 HTTP 请求和响应的日志
 * 
 * @author Network Service Template
 */
public class LoggingMiddleware implements HttpMiddleware {

    private static final Logger log = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public int getPriority() {
        return 20; // 较高优先级
    }

    @Override
    @Nullable
    public HttpResponse beforeRequest(@NotNull HttpRequest request) {
        log.info("📥 HTTP Request: {} {} from client {}", request.getMethod(), request.getPath(),
                request.getClientId());

        // 记录请求详情
        if (log.isDebugEnabled()) {
            log.debug("Request details: {}", request);
            log.debug("Request headers: {}", request.getHeaders());
            if (request.getBody() != null) {
                log.debug("Request body: {}", request.getBody());
            }
        }

        return null;
    }

    @Override
    @Nullable
    public HttpResponse afterRequest(@NotNull HttpRequest request, @NotNull HttpResponse response) {
        log.info("📤 HTTP Response: {} {} -> {} ({}ms)", request.getMethod(), request.getPath(),
                response.getStatusCode(), System.currentTimeMillis() - request.getTimestamp());

        // 记录响应详情
        if (log.isDebugEnabled()) {
            log.debug("Response details: {}", response);
            log.debug("Response headers: {}", response.getHeaders());
            if (response.getBody() != null) {
                log.debug("Response body: {}", response.getBody());
            }
        }

        return null;
    }
}
