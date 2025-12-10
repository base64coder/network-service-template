package com.dtc.core.network.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP 响应处理器
 * 处理 HTTP 响应的创建、序列化和发送
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

    private final ObjectMapper objectMapper;
    private final AtomicLong sentResponses = new AtomicLong(0);
    private final AtomicLong errorResponses = new AtomicLong(0);

    @Inject
    public HttpResponseHandler() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 发送响应
     * 
     * @param clientId 客户端ID
     * @param response HTTP 响应
     */
    public void sendResponse(@NotNull String clientId, @NotNull HttpResponseEx response) {
        try {
            sentResponses.incrementAndGet();

            if (response.isServerError()) {
                errorResponses.incrementAndGet();
            }

            log.debug("Sending HTTP response to client: {} (status: {})", clientId, response.getStatusCode());

            // 可以通过路由管理器实现更复杂的响应发送逻辑
            // 例如通过 Netty Channel 发送响应
        } catch (Exception e) {
            log.error("Error sending HTTP response to client: {}", clientId, e);
        }
    }

    /**
     * 创建成功响应
     */
    @NotNull
    public HttpResponseEx createSuccessResponse(@Nullable Object data) {
        return new HttpResponseEx.Builder().ok().jsonContent().body(serializeToJson(data)).build();
    }

    /**
     * 创建创建成功响应
     */
    @NotNull
    public HttpResponseEx createCreatedResponse(@Nullable Object data) {
        return new HttpResponseEx.Builder().created().jsonContent().body(serializeToJson(data)).build();
    }

    /**
     * 创建无内容响应
     */
    @NotNull
    public HttpResponseEx createNoContentResponse() {
        return new HttpResponseEx.Builder().noContent().build();
    }

    /**
     * 创建错误响应
     */
    @NotNull
    public HttpResponseEx createErrorResponse(int statusCode, @NotNull String error, @Nullable String message) {
        return new HttpResponseEx.Builder().statusCode(statusCode).statusMessage(error).jsonContent()
                .body(createErrorJson(error, message)).build();
    }

    /**
     * 创建 400 错误响应
     */
    @NotNull
    public HttpResponseEx createBadRequestResponse(@Nullable String message) {
        return createErrorResponse(400, "Bad Request", message);
    }

    /**
     * 创建 401 错误响应
     */
    @NotNull
    public HttpResponseEx createUnauthorizedResponse(@Nullable String message) {
        return createErrorResponse(401, "Unauthorized", message);
    }

    /**
     * 创建 403 错误响应
     */
    @NotNull
    public HttpResponseEx createForbiddenResponse(@Nullable String message) {
        return createErrorResponse(403, "Forbidden", message);
    }

    /**
     * 创建 404 错误响应
     */
    @NotNull
    public HttpResponseEx createNotFoundResponse(@Nullable String message) {
        return createErrorResponse(404, "Not Found", message);
    }

    /**
     * 创建 405 错误响应
     */
    @NotNull
    public HttpResponseEx createMethodNotAllowedResponse(@Nullable String message) {
        return createErrorResponse(405, "Method Not Allowed", message);
    }

    /**
     * 创建 409 错误响应
     */
    @NotNull
    public HttpResponseEx createConflictResponse(@Nullable String message) {
        return createErrorResponse(409, "Conflict", message);
    }

    /**
     * 创建 500 错误响应
     */
    @NotNull
    public HttpResponseEx createInternalServerErrorResponse(@Nullable String message) {
        return createErrorResponse(500, "Internal Server Error", message);
    }

    /**
     * 创建 JSON 响应
     */
    @NotNull
    public HttpResponseEx createJsonResponse(int statusCode, @Nullable Object data) {
        return new HttpResponseEx.Builder().statusCode(statusCode).jsonContent().body(serializeToJson(data)).build();
    }

    /**
     * 创建文本响应
     */
    @NotNull
    public HttpResponseEx createTextResponse(int statusCode, @NotNull String text) {
        return new HttpResponseEx.Builder().statusCode(statusCode).textContent().body(text).build();
    }

    /**
     * 创建 HTML 响应
     */
    @NotNull
    public HttpResponseEx createHtmlResponse(int statusCode, @NotNull String html) {
        return new HttpResponseEx.Builder().statusCode(statusCode).htmlContent().body(html).build();
    }

    /**
     * 创建 XML 响应
     */
    @NotNull
    public HttpResponseEx createXmlResponse(int statusCode, @NotNull String xml) {
        return new HttpResponseEx.Builder().statusCode(statusCode).xmlContent().body(xml).build();
    }

    /**
     * 创建重定向响应
     */
    @NotNull
    public HttpResponseEx createRedirectResponse(@NotNull String location) {
        return new HttpResponseEx.Builder().statusCode(302).statusMessage("Found").addHeader("Location", location)
                .build();
    }

    /**
     * 创建永久重定向响应
     */
    @NotNull
    public HttpResponseEx createPermanentRedirectResponse(@NotNull String location) {
        return new HttpResponseEx.Builder().statusCode(301).statusMessage("Moved Permanently")
                .addHeader("Location", location).build();
    }

    /**
     * 序列化对象为 JSON
     */
    @NotNull
    public String serializeToJson(@Nullable Object data) {
        if (data == null) {
            return "null";
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error serializing object to JSON", e);
            return "{}";
        }
    }

    /**
     * 创建错误 JSON
     */
    @NotNull
    private String createErrorJson(@NotNull String error, @Nullable String message) {
        try {
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("error", error);
            errorMap.put("message", message != null ? message : "");
            errorMap.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(errorMap);
        } catch (Exception e) {
            return "{\"error\":\"" + error + "\",\"message\":\"" + (message != null ? message : "") + "\"}";
        }
    }

    /**
     * 添加 CORS 头部
     */
    @NotNull
    public HttpResponseEx addCorsHeaders(@NotNull HttpResponseEx response) {
        return new HttpResponseEx.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("Access-Control-Allow-Origin", "*")
                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization").body(response.getBody())
                .contentType(response.getContentType()).build();
    }

    /**
     * 添加缓存头部
     */
    @NotNull
    public HttpResponseEx addCacheHeaders(@NotNull HttpResponseEx response, int maxAge) {
        return new HttpResponseEx.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("Cache-Control", "public, max-age=" + maxAge)
                .addHeader("Expires", new java.util.Date(System.currentTimeMillis() + maxAge * 1000L).toString())
                .body(response.getBody()).contentType(response.getContentType()).build();
    }

    /**
     * 添加安全头部
     */
    @NotNull
    public HttpResponseEx addSecurityHeaders(@NotNull HttpResponseEx response) {
        return new HttpResponseEx.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("X-Content-Type-Options", "nosniff").addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block")
                .addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains").body(response.getBody())
                .contentType(response.getContentType()).build();
    }

    /**
     * 获取响应统计信息
     */
    @NotNull
    public HttpResponseStats getStats() {
        return new HttpResponseStats(sentResponses.get(), errorResponses.get());
    }

    /**
     * HTTP 响应统计信息
     */
    public static class HttpResponseStats {
        private final long sentResponses;
        private final long errorResponses;

        public HttpResponseStats(long sentResponses, long errorResponses) {
            this.sentResponses = sentResponses;
            this.errorResponses = errorResponses;
        }

        public long getSentResponses() {
            return sentResponses;
        }

        public long getErrorResponses() {
            return errorResponses;
        }

        public double getErrorRate() {
            return sentResponses > 0 ? (double) errorResponses / sentResponses : 0.0;
        }

        @Override
        public String toString() {
            return String.format("HttpResponseStats{sent=%d, errors=%d, errorRate=%.2f%%}", sentResponses,
                    errorResponses, getErrorRate() * 100);
        }
    }
}
