package com.dtc.core.http.route;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.HttpRequestEx;
import com.dtc.core.http.HttpResponseEx;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 路由 表示一个 HTTP 路由配置
 * 
 * @author Network Service Template
 */
public class HttpRoute {

    private final String method;
    private final String path;
    private final HttpRouteHandler handler;
    private final Pattern compiledPattern;
    private final String[] pathSegments;

    public HttpRoute(@NotNull String method, @NotNull String path, @NotNull HttpRouteHandler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.compiledPattern = compilePathPattern(path);
        this.pathSegments = path.split("/");
    }

    /**
     * 获取 HTTP 方法
     */
    @NotNull
    public String getMethod() {
        return method;
    }

    /**
     * 获取路径模式
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * 获取处理器
     */
    @NotNull
    public HttpRouteHandler getHandler() {
        return handler;
    }

    /**
     * 检查是否匹配路径
     */
    public boolean matches(@NotNull String requestPath) {
        if (compiledPattern != null) {
            return compiledPattern.matcher(requestPath).matches();
        }
        return path.equals(requestPath);
    }

    /**
     * 提取路径参数
     */
    @NotNull
    public Map<String, String> extractPathParameters(@NotNull String requestPath) {
        Map<String, String> parameters = new HashMap<>();

        if (compiledPattern != null) {
            Matcher matcher = compiledPattern.matcher(requestPath);
            if (matcher.matches()) {
                // 提取参数名
                String[] requestSegments = requestPath.split("/");
                for (int i = 0; i < pathSegments.length && i < requestSegments.length; i++) {
                    String segment = pathSegments[i];
                    if (segment.startsWith("{") && segment.endsWith("}")) {
                        String paramName = segment.substring(1, segment.length() - 1);
                        parameters.put(paramName, requestSegments[i]);
                    }
                }
            }
        }

        return parameters;
    }

    /**
     * 编译路径模式为正则表达式
     */
    @NotNull
    private Pattern compilePathPattern(@NotNull String path) {
        if (path.contains("{")) {
            // 将 {param} 替换为捕获组
            String regex = path.replaceAll("\\{[^}]+\\}", "([^/]+)");
            return Pattern.compile("^" + regex + "$");
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("HttpRoute{method='%s', path='%s'}", method, path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        HttpRoute other = (HttpRoute) obj;
        return method.equals(other.method) && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return method.hashCode() * 31 + path.hashCode();
    }
}
