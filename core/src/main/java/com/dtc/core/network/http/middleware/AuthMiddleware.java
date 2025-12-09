package com.dtc.core.network.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * 认证中间件
 * 处理 HTTP 请求的认证和授权
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
        return 30; // 认证中间件优先级较高
    }

    @Override
    @Nullable
    public HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        String path = request.getPath();

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            return null; // 允许访问
        }

        // 检查认证头
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
    public HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        // 可以通过路由管理器实现响应后的认证处理逻辑
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
        // 可以通过路由管理器实现更复杂的令牌验证逻辑
        // 例如使用 JWT 验证或数据库查询
        return token.length() > 10; // 简单的令牌验证
    }

    /**
     * 检查是否为管理员令牌
     */
    private boolean isAdminToken(@NotNull String token) {
        // 可以通过路由管理器实现更复杂的管理员令牌检查逻辑
        return token.startsWith("admin_"); // 简单的管理员检查
    }

    /**
     * 创建未授权响应
     */
    @NotNull
    private HttpResponseEx createUnauthorizedResponse(@NotNull String message) {
        return new HttpResponseEx.Builder().statusCode(401).statusMessage("Unauthorized").jsonContent()
                .body("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}").build();
    }

    /**
     * 创建禁止访问响应
     */
    @NotNull
    private HttpResponseEx createForbiddenResponse(@NotNull String message) {
        return new HttpResponseEx.Builder().statusCode(403).statusMessage("Forbidden").jsonContent()
                .body("{\"error\":\"Forbidden\",\"message\":\"" + message + "\"}").build();
    }
}
