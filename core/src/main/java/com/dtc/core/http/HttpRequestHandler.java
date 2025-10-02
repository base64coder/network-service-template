package com.dtc.core.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.route.HttpRoute;
import com.dtc.core.http.middleware.HttpMiddleware;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP 请求处理器 处理 HTTP 请求的路由、中间件、参数解析等
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private final HttpRouteManager routeManager;
    private final HttpMiddlewareManager middlewareManager;
    private final ObjectMapper objectMapper;
    private final AtomicLong processedRequests = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    @Inject
    public HttpRequestHandler(@NotNull HttpRouteManager routeManager,
            @NotNull HttpMiddlewareManager middlewareManager) {
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 处理 HTTP 请求
     * 
     * @param request HTTP 请求
     * @return HTTP 响应
     */
    @NotNull
    public HttpResponse handleRequest(@NotNull HttpRequest request) {
        try {
            processedRequests.incrementAndGet();

            log.debug("Processing HTTP request: {} {}", request.getMethod(), request.getPath());

            // 执行前置中间件
            HttpResponse preResponse = executePreMiddleware(request);
            if (preResponse != null) {
                return preResponse;
            }

            // 查找匹配的路由
            HttpRoute route = routeManager.findRoute(request.getMethod(), request.getPath());
            if (route == null) {
                return createNotFoundResponse(request);
            }

            // 提取路径参数
            extractPathParameters(request, route);

            // 执行路由处理器
            HttpResponse response = route.getHandler().handle(request);
            if (response == null) {
                response = createInternalServerErrorResponse("Route handler returned null");
            }

            // 执行后置中间件
            response = executePostMiddleware(request, response);

            log.debug("HTTP request processed successfully: {} {} -> {}", request.getMethod(), request.getPath(),
                    response.getStatusCode());

            return response;

        } catch (Exception e) {
            log.error("Error processing HTTP request: {} {}", request.getMethod(), request.getPath(), e);
            errorCount.incrementAndGet();
            return createInternalServerErrorResponse(e.getMessage());
        }
    }

    /**
     * 执行前置中间件
     */
    @Nullable
    private HttpResponse executePreMiddleware(@NotNull HttpRequest request) {
        List<HttpMiddleware> middlewares = middlewareManager.getMiddlewares();

        for (HttpMiddleware middleware : middlewares) {
            try {
                HttpResponse response = middleware.beforeRequest(request);
                if (response != null) {
                    log.debug("Middleware {} intercepted request", middleware.getClass().getSimpleName());
                    return response;
                }
            } catch (Exception e) {
                log.error("Error in pre-middleware: {}", middleware.getClass().getSimpleName(), e);
                return createInternalServerErrorResponse("Middleware error: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * 执行后置中间件
     */
    @NotNull
    private HttpResponse executePostMiddleware(@NotNull HttpRequest request, @NotNull HttpResponse response) {
        List<HttpMiddleware> middlewares = middlewareManager.getMiddlewares();

        HttpResponse currentResponse = response;

        for (HttpMiddleware middleware : middlewares) {
            try {
                HttpResponse newResponse = middleware.afterRequest(request, currentResponse);
                if (newResponse != null) {
                    currentResponse = newResponse;
                }
            } catch (Exception e) {
                log.error("Error in post-middleware: {}", middleware.getClass().getSimpleName(), e);
                // 继续执行其他中间件，不中断流程
            }
        }

        return currentResponse;
    }

    /**
     * 提取路径参数
     */
    private void extractPathParameters(@NotNull HttpRequest request, @NotNull HttpRoute route) {
        Map<String, String> pathParams = route.extractPathParameters(request.getPath());
        if (pathParams != null) {
            request.getPathParameters().putAll(pathParams);
        }
    }

    /**
     * 解析 JSON 请求体
     */
    @Nullable
    public <T> T parseJsonBody(@NotNull HttpRequest request, @NotNull Class<T> clazz) {
        if (!request.isJsonContent() || request.getBody() == null) {
            return null;
        }

        try {
            return objectMapper.readValue(request.getBody(), clazz);
        } catch (Exception e) {
            log.error("Error parsing JSON body", e);
            return null;
        }
    }

    /**
     * 解析查询参数
     */
    @NotNull
    public Map<String, String> parseQueryParameters(@NotNull HttpRequest request) {
        return request.getQueryParameters();
    }

    /**
     * 解析表单参数
     */
    @NotNull
    public Map<String, String> parseFormParameters(@NotNull HttpRequest request) {
        // 这里可以实现表单参数解析逻辑
        return new java.util.HashMap<>();
    }

    /**
     * 验证请求
     */
    public boolean validateRequest(@NotNull HttpRequest request) {
        // 基本验证
        if (request.getMethod() == null || request.getPath() == null) {
            return false;
        }

        // 检查必需的头部
        if (request.getHeader("Host") == null) {
            return false;
        }

        return true;
    }

    /**
     * 创建 404 响应
     */
    @NotNull
    private HttpResponse createNotFoundResponse(@NotNull HttpRequest request) {
        return new HttpResponse.Builder().notFound().jsonContent()
                .body(createErrorJson("Not Found", "The requested resource was not found")).build();
    }

    /**
     * 创建 500 响应
     */
    @NotNull
    private HttpResponse createInternalServerErrorResponse(@Nullable String message) {
        return new HttpResponse.Builder().internalServerError().jsonContent()
                .body(createErrorJson("Internal Server Error", message)).build();
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
     * 获取处理统计信息
     */
    @NotNull
    public HttpRequestStats getStats() {
        return new HttpRequestStats(processedRequests.get(), errorCount.get());
    }

    /**
     * HTTP 请求统计信息
     */
    public static class HttpRequestStats {
        private final long processedRequests;
        private final long errorCount;

        public HttpRequestStats(long processedRequests, long errorCount) {
            this.processedRequests = processedRequests;
            this.errorCount = errorCount;
        }

        public long getProcessedRequests() {
            return processedRequests;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public double getErrorRate() {
            return processedRequests > 0 ? (double) errorCount / processedRequests : 0.0;
        }

        @Override
        public String toString() {
            return String.format("HttpRequestStats{processed=%d, errors=%d, errorRate=%.2f%%}", processedRequests,
                    errorCount, getErrorRate() * 100);
        }
    }
}
