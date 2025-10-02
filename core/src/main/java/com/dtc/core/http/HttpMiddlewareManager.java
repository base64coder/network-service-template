package com.dtc.core.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.middleware.HttpMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * HTTP 中间件管理器 管理 HTTP 中间件的注册、执行等
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpMiddlewareManager {

    private static final Logger log = LoggerFactory.getLogger(HttpMiddlewareManager.class);

    private final List<HttpMiddleware> middlewares = new CopyOnWriteArrayList<>();

    @Inject
    public HttpMiddlewareManager() {
        log.info("HTTP Middleware Manager initialized");
    }

    /**
     * 添加中间件
     * 
     * @param middleware 中间件
     */
    public void addMiddleware(@NotNull HttpMiddleware middleware) {
        middlewares.add(middleware);
        log.debug("Added middleware: {}", middleware.getClass().getSimpleName());
    }

    /**
     * 移除中间件
     * 
     * @param middleware 中间件
     */
    public boolean removeMiddleware(@NotNull HttpMiddleware middleware) {
        boolean removed = middlewares.remove(middleware);
        if (removed) {
            log.debug("Removed middleware: {}", middleware.getClass().getSimpleName());
        }
        return removed;
    }

    /**
     * 获取所有中间件
     */
    @NotNull
    public List<HttpMiddleware> getMiddlewares() {
        return new ArrayList<>(middlewares);
    }

    /**
     * 清除所有中间件
     */
    public void clearMiddlewares() {
        middlewares.clear();
        log.info("All middlewares cleared");
    }

    /**
     * 获取中间件数量
     */
    public int getMiddlewareCount() {
        return middlewares.size();
    }

    /**
     * 检查是否包含指定中间件
     */
    public boolean containsMiddleware(@NotNull HttpMiddleware middleware) {
        return middlewares.contains(middleware);
    }

    /**
     * 检查是否包含指定类型的中间件
     */
    public boolean containsMiddleware(@NotNull Class<? extends HttpMiddleware> middlewareClass) {
        return middlewares.stream().anyMatch(middleware -> middlewareClass.isInstance(middleware));
    }

    /**
     * 获取指定类型的中间件
     */
    @NotNull
    public <T extends HttpMiddleware> List<T> getMiddlewares(@NotNull Class<T> middlewareClass) {
        List<T> result = new ArrayList<>();
        for (HttpMiddleware middleware : middlewares) {
            if (middlewareClass.isInstance(middleware)) {
                result.add(middlewareClass.cast(middleware));
            }
        }
        return result;
    }

    /**
     * 获取中间件统计信息
     */
    @NotNull
    public MiddlewareStats getStats() {
        return new MiddlewareStats(middlewares.size());
    }

    /**
     * 中间件统计信息
     */
    public static class MiddlewareStats {
        private final int middlewareCount;

        public MiddlewareStats(int middlewareCount) {
            this.middlewareCount = middlewareCount;
        }

        public int getMiddlewareCount() {
            return middlewareCount;
        }

        @Override
        public String toString() {
            return String.format("MiddlewareStats{count=%d}", middlewareCount);
        }
    }
}
