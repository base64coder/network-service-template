package com.dtc.core.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;

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
    public HttpResponse beforeRequest(@NotNull HttpRequest request) {
        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createPreflightResponse();
        }

        return null;
    }

    @Override
    @Nullable
    public HttpResponse afterRequest(@NotNull HttpRequest request, @NotNull HttpResponse response) {
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
    private HttpResponse createPreflightResponse() {
        return new HttpResponse.Builder().statusCode(200).statusMessage("OK")
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders).addHeader("Access-Control-Max-Age", "86400")
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials)).build();
    }

    /**
     * 添加 CORS 头部
     */
    @NotNull
    private HttpResponse addCorsHeaders(@NotNull HttpResponse response) {
        return new HttpResponse.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders)
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials))
                .body(response.getBody()).contentType(response.getContentType()).build();
    }
}
