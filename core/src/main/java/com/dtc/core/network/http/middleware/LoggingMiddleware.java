package com.dtc.core.network.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志记录中间件
 * 记录 HTTP 请求和响应的详细信息
 * 
 * @author Network Service Template
 */
public class LoggingMiddleware implements HttpMiddleware {

    private static final Logger log = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public int getPriority() {
        return 20; // 日志记录优先级较低
    }

    @Override
    @Nullable
    public HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        log.info("📥 Received HTTP Request: {} {} from client {}", request.getMethod(), request.getPath(),
                request.getClientId());

        // 记录请求详细信息
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
    public HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        log.info("📤 Sent HTTP Response: {} {} -> {} ({}ms)", request.getMethod(), request.getPath(),
                response.getStatusCode(), System.currentTimeMillis() - request.getTimestamp());

        // 记录响应详细信息
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
