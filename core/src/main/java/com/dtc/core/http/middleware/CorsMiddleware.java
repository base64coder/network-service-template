package com.dtc.core.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.HttpRequestEx;
import com.dtc.core.http.HttpResponseEx;

/**
 * CORS 中间件 处理跨域资源共享
 * 
 * @author Network Service Template
 */
public class CorsMiddleware implements HttpMiddleware {

    private final String allowedOrigins;
    private final String allowedMethods;
    private final String allowedHeaders;
    private final boolean allowCredentials;

    public CorsMiddleware() {
        this("*", "GET, POST, PUT, DELETE, OPTIONS", "Content-Type, Authorization", false);
    }

    public CorsMiddleware(@NotNull String allowedOrigins, @NotNull String allowedMethods,
            @NotNull String allowedHeaders, boolean allowCredentials) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.allowCredentials = allowCredentials;
    }

    @Override
    @Nullable
    public HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createPreflightResponse();
        }

        return null;
    }

    @Override
    @Nullable
    public HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        // 添加 CORS 头部
        return addCorsHeaders(response);
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级
    }

    /**
     * 创建预检响应
     */
    @NotNull
    private HttpResponseEx createPreflightResponse() {
        return new HttpResponseEx.Builder().statusCode(200).statusMessage("OK")
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders).addHeader("Access-Control-Max-Age", "86400")
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials)).build();
    }

    /**
     * 添加 CORS 头部
     */
    @NotNull
    private HttpResponseEx addCorsHeaders(@NotNull HttpResponseEx response) {
        return new HttpResponseEx.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders)
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials))
                .body(response.getBody()).contentType(response.getContentType()).build();
    }
}
