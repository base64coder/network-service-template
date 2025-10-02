package com.dtc.core.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.route.HttpRoute;
import com.dtc.core.http.route.HttpRouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 路由管理器 管理 HTTP 路由的注册、查找、匹配等
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpRouteManager {

    private static final Logger log = LoggerFactory.getLogger(HttpRouteManager.class);

    private final Map<String, List<HttpRoute>> routes = new ConcurrentHashMap<>();
    private final Map<String, Pattern> compiledPatterns = new ConcurrentHashMap<>();

    @Inject
    public HttpRouteManager() {
        log.info("HTTP Route Manager initialized");
    }

    /**
     * 注册路由
     * 
     * @param method  HTTP 方法
     * @param path    路径模式
     * @param handler 处理器
     */
    public void registerRoute(@NotNull String method, @NotNull String path, @NotNull HttpRouteHandler handler) {
        String normalizedMethod = method.toUpperCase();
        String normalizedPath = normalizePath(path);

        HttpRoute route = new HttpRoute(normalizedMethod, normalizedPath, handler);

        routes.computeIfAbsent(normalizedMethod, k -> new ArrayList<>()).add(route);

        // 编译路径模式
        compilePathPattern(normalizedPath);

        log.debug("Registered route: {} {}", normalizedMethod, normalizedPath);
    }

    /**
     * 注册 GET 路由
     */
    public void registerGet(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("GET", path, handler);
    }

    /**
     * 注册 POST 路由
     */
    public void registerPost(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("POST", path, handler);
    }

    /**
     * 注册 PUT 路由
     */
    public void registerPut(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("PUT", path, handler);
    }

    /**
     * 注册 DELETE 路由
     */
    public void registerDelete(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("DELETE", path, handler);
    }

    /**
     * 注册 PATCH 路由
     */
    public void registerPatch(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("PATCH", path, handler);
    }

    /**
     * 注册 OPTIONS 路由
     */
    public void registerOptions(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("OPTIONS", path, handler);
    }

    /**
     * 注册 HEAD 路由
     */
    public void registerHead(@NotNull String path, @NotNull HttpRouteHandler handler) {
        registerRoute("HEAD", path, handler);
    }

    /**
     * 查找匹配的路由
     * 
     * @param method HTTP 方法
     * @param path   请求路径
     * @return 匹配的路由，如果没有找到则返回 null
     */
    @Nullable
    public HttpRoute findRoute(@NotNull String method, @NotNull String path) {
        String normalizedMethod = method.toUpperCase();
        String normalizedPath = normalizePath(path);

        List<HttpRoute> methodRoutes = routes.get(normalizedMethod);
        if (methodRoutes == null) {
            return null;
        }

        // 按优先级排序（精确匹配优先）
        methodRoutes.sort((r1, r2) -> {
            boolean exact1 = !r1.getPath().contains("{");
            boolean exact2 = !r2.getPath().contains("{");
            if (exact1 && !exact2)
                return -1;
            if (!exact1 && exact2)
                return 1;
            return 0;
        });

        for (HttpRoute route : methodRoutes) {
            if (matchesRoute(route, normalizedPath)) {
                log.debug("Found matching route: {} {}", route.getMethod(), route.getPath());
                return route;
            }
        }

        return null;
    }

    /**
     * 检查路由是否匹配
     */
    private boolean matchesRoute(@NotNull HttpRoute route, @NotNull String path) {
        String routePath = route.getPath();

        // 精确匹配
        if (routePath.equals(path)) {
            return true;
        }

        // 模式匹配
        if (routePath.contains("{")) {
            Pattern pattern = compiledPatterns.get(routePath);
            if (pattern != null) {
                return pattern.matcher(path).matches();
            }
        }

        return false;
    }

    /**
     * 编译路径模式
     */
    private void compilePathPattern(@NotNull String path) {
        if (path.contains("{")) {
            String regex = path.replaceAll("\\{[^}]+\\}", "([^/]+)");
            Pattern pattern = Pattern.compile("^" + regex + "$");
            compiledPatterns.put(path, pattern);
        }
    }

    /**
     * 标准化路径
     */
    @NotNull
    private String normalizePath(@NotNull String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * 获取所有路由
     */
    @NotNull
    public Map<String, List<HttpRoute>> getAllRoutes() {
        return new HashMap<>(routes);
    }

    /**
     * 获取指定方法的路由
     */
    @NotNull
    public List<HttpRoute> getRoutes(@NotNull String method) {
        return routes.getOrDefault(method.toUpperCase(), new ArrayList<>());
    }

    /**
     * 获取路由统计信息
     */
    @NotNull
    public RouteStats getStats() {
        int totalRoutes = routes.values().stream().mapToInt(List::size).sum();
        Map<String, Integer> methodCounts = new HashMap<>();

        for (Map.Entry<String, List<HttpRoute>> entry : routes.entrySet()) {
            methodCounts.put(entry.getKey(), entry.getValue().size());
        }

        return new RouteStats(totalRoutes, methodCounts);
    }

    /**
     * 清除所有路由
     */
    public void clearRoutes() {
        routes.clear();
        compiledPatterns.clear();
        log.info("All routes cleared");
    }

    /**
     * 移除指定方法的所有路由
     */
    public void removeRoutes(@NotNull String method) {
        String normalizedMethod = method.toUpperCase();
        List<HttpRoute> removed = routes.remove(normalizedMethod);
        if (removed != null) {
            log.info("Removed {} routes for method {}", removed.size(), normalizedMethod);
        }
    }

    /**
     * 移除指定路由
     */
    public boolean removeRoute(@NotNull String method, @NotNull String path) {
        String normalizedMethod = method.toUpperCase();
        String normalizedPath = normalizePath(path);

        List<HttpRoute> methodRoutes = routes.get(normalizedMethod);
        if (methodRoutes != null) {
            boolean removed = methodRoutes.removeIf(route -> route.getPath().equals(normalizedPath));
            if (removed) {
                compiledPatterns.remove(normalizedPath);
                log.debug("Removed route: {} {}", normalizedMethod, normalizedPath);
            }
            return removed;
        }

        return false;
    }

    /**
     * 路由统计信息
     */
    public static class RouteStats {
        private final int totalRoutes;
        private final Map<String, Integer> methodCounts;

        public RouteStats(int totalRoutes, Map<String, Integer> methodCounts) {
            this.totalRoutes = totalRoutes;
            this.methodCounts = methodCounts;
        }

        public int getTotalRoutes() {
            return totalRoutes;
        }

        public Map<String, Integer> getMethodCounts() {
            return methodCounts;
        }

        @Override
        public String toString() {
            return String.format("RouteStats{totalRoutes=%d, methodCounts=%s}", totalRoutes, methodCounts);
        }
    }
}
