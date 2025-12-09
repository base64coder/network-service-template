package com.dtc.core.network.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制中间件
 * 根据客户端 IP 对请求进行速率限制
 * 
 * @author Network Service Template
 */
public class RateLimitMiddleware implements HttpMiddleware {

    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimitMiddleware() {
        this(100, 60000); // 默认：每分钟 100 个请求
    }

    public RateLimitMiddleware(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    @Override
    public int getPriority() {
        return 40; // 速率限制优先级较高
    }

    @Override
    @Nullable
    public HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        String clientId = request.getClientId();
        long currentTime = System.currentTimeMillis();

        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientId, k -> new RateLimitInfo(currentTime));

        // 检查时间窗口是否过期
        if (currentTime - rateLimitInfo.getWindowStart() > windowMs) {
            // 重置时间窗口
            rateLimitInfo.reset(currentTime);
        }

        // 检查请求数量
        if (rateLimitInfo.getRequestCount() >= maxRequests) {
            return createRateLimitResponse();
        }

        // 增加请求计数
        rateLimitInfo.incrementRequest();

        return null;
    }

    @Override
    @Nullable
    public HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        // 可以通过路由管理器实现响应后的速率限制处理逻辑
        return null;
    }

    /**
     * 创建速率限制响应
     */
    @NotNull
    private HttpResponseEx createRateLimitResponse() {
        return new HttpResponseEx.Builder().statusCode(429).statusMessage("Too Many Requests").jsonContent()
                .body("{\"error\":\"Rate Limit Exceeded\",\"message\":\"Too many requests\"}")
                .addHeader("Retry-After", String.valueOf(windowMs / 1000)).build();
    }

    /**
     * 清理过期的速率限制信息
     */
    public void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        rateLimitMap.entrySet().removeIf(entry -> currentTime - entry.getValue().getWindowStart() > windowMs);
    }

    /**
     * 获取活动客户端数量
     */
    public int getActiveClients() {
        return rateLimitMap.size();
    }

    /**
     * 速率限制信息
     */
    private static class RateLimitInfo {
        private long windowStart;
        private final AtomicInteger requestCount = new AtomicInteger(0);

        public RateLimitInfo(long windowStart) {
            this.windowStart = windowStart;
        }

        public long getWindowStart() {
            return windowStart;
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public void incrementRequest() {
            requestCount.incrementAndGet();
        }

        public void reset(long newWindowStart) {
            this.windowStart = newWindowStart;
            this.requestCount.set(0);
        }
    }
}
