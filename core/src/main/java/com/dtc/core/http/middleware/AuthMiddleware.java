package com.dtc.core.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;

/**
 * 认证中间件 处理 HTTP 请求的认证和授权
 * 
 * @author Network Service Template
 */
public class AuthMiddleware implements HttpMiddleware {

    private final String[] publicPaths;
    private final String[] adminPaths;

    public AuthMiddleware() {
        this(new String[] { "/", "/health", "/status", "/api/info" }, new String[] { "/api/admin" });
    }

    public AuthMiddleware(@NotNull String[] publicPaths, @NotNull String[] adminPaths) {
        this.publicPaths = publicPaths;
        this.adminPaths = adminPaths;
    }

    @Override
    public int getPriority() {
        return 30; // 中等优先级
    }

    @Override
    @Nullable
    public HttpResponse beforeRequest(@NotNull HttpRequest request) {
        String path = request.getPath();

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            return null; // 允许访问
        }

        // 检查认证
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return createUnauthorizedResponse("Missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        if (!isValidToken(token)) {
            return createUnauthorizedResponse("Invalid token");
        }

        // 检查管理员路径
        if (isAdminPath(path) && !isAdminToken(token)) {
            return createForbiddenResponse("Admin access required");
        }

        return null;
    }

    @Override
    @Nullable
    public HttpResponse afterRequest(@NotNull HttpRequest request, @NotNull HttpResponse response) {
        // 可以在这里添加响应后的认证处理逻辑
        return null;
    }

    /**
     * 检查是否为公开路径
     */
    private boolean isPublicPath(@NotNull String path) {
        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为管理员路径
     */
    private boolean isAdminPath(@NotNull String path) {
        for (String adminPath : adminPaths) {
            if (path.startsWith(adminPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证令牌
     */
    private boolean isValidToken(@NotNull String token) {
        // 这里应该实现真实的令牌验证逻辑
        // 例如：JWT 验证、数据库查询等
        return token.length() > 10; // 简单的示例验证
    }

    /**
     * 检查是否为管理员令牌
     */
    private boolean isAdminToken(@NotNull String token) {
        // 这里应该实现真实的管理员令牌检查逻辑
        return token.startsWith("admin_"); // 简单的示例检查
    }

    /**
     * 创建未授权响应
     */
    @NotNull
    private HttpResponse createUnauthorizedResponse(@NotNull String message) {
        return new HttpResponse.Builder().statusCode(401).statusMessage("Unauthorized").jsonContent()
                .body("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}").build();
    }

    /**
     * 创建禁止访问响应
     */
    @NotNull
    private HttpResponse createForbiddenResponse(@NotNull String message) {
        return new HttpResponse.Builder().statusCode(403).statusMessage("Forbidden").jsonContent()
                .body("{\"error\":\"Forbidden\",\"message\":\"" + message + "\"}").build();
    }
}
