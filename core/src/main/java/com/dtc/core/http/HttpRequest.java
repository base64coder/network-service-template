package com.dtc.core.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP 请求模型 封装 HTTP 请求的所有信息
 * 
 * @author Network Service Template
 */
public class HttpRequest {

    private final String method;
    private final String path;
    private final String uri;
    private final HttpVersion version;
    private final Map<String, String> headers;
    private final Map<String, String> queryParameters;
    private final Map<String, String> pathParameters;
    private final String body;
    private final String contentType;
    private final String clientId;
    private final long timestamp;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.uri = builder.uri;
        this.version = builder.version;
        this.headers = new ConcurrentHashMap<>(builder.headers);
        this.queryParameters = new ConcurrentHashMap<>(builder.queryParameters);
        this.pathParameters = new ConcurrentHashMap<>(builder.pathParameters);
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.clientId = builder.clientId;
        this.timestamp = builder.timestamp;
    }

    // Getters
    @NotNull
    public String getMethod() {
        return method;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getUri() {
        return uri;
    }

    @NotNull
    public HttpVersion getVersion() {
        return version;
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @NotNull
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    @NotNull
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    @Nullable
    public String getContentType() {
        return contentType;
    }

    @NotNull
    public String getClientId() {
        return clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // 便捷方法
    @Nullable
    public String getHeader(@NotNull String name) {
        return headers.get(name.toLowerCase());
    }

    @Nullable
    public String getQueryParameter(@NotNull String name) {
        return queryParameters.get(name);
    }

    @Nullable
    public String getPathParameter(@NotNull String name) {
        return pathParameters.get(name);
    }

    public boolean hasHeader(@NotNull String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public boolean hasQueryParameter(@NotNull String name) {
        return queryParameters.containsKey(name);
    }

    public boolean hasPathParameter(@NotNull String name) {
        return pathParameters.containsKey(name);
    }

    public boolean isGet() {
        return "GET".equalsIgnoreCase(method);
    }

    public boolean isPost() {
        return "POST".equalsIgnoreCase(method);
    }

    public boolean isPut() {
        return "PUT".equalsIgnoreCase(method);
    }

    public boolean isDelete() {
        return "DELETE".equalsIgnoreCase(method);
    }

    public boolean isPatch() {
        return "PATCH".equalsIgnoreCase(method);
    }

    public boolean isOptions() {
        return "OPTIONS".equalsIgnoreCase(method);
    }

    public boolean isHead() {
        return "HEAD".equalsIgnoreCase(method);
    }

    public boolean isJsonContent() {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    public boolean isFormContent() {
        return contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded");
    }

    public boolean isMultipartContent() {
        return contentType != null && contentType.toLowerCase().contains("multipart/form-data");
    }

    @Override
    public String toString() {
        return String.format("HttpRequest{method='%s', path='%s', clientId='%s', timestamp=%d}", method, path, clientId,
                timestamp);
    }

    /**
     * 构建器
     */
    public static class Builder {
        private String method;
        private String path;
        private String uri;
        private HttpVersion version;
        private Map<String, String> headers = new ConcurrentHashMap<>();
        private Map<String, String> queryParameters = new ConcurrentHashMap<>();
        private Map<String, String> pathParameters = new ConcurrentHashMap<>();
        private String body;
        private String contentType;
        private String clientId;
        private long timestamp;

        public Builder method(@NotNull String method) {
            this.method = method;
            return this;
        }

        public Builder path(@NotNull String path) {
            this.path = path;
            return this;
        }

        public Builder uri(@NotNull String uri) {
            this.uri = uri;
            return this;
        }

        public Builder version(@NotNull HttpVersion version) {
            this.version = version;
            return this;
        }

        public Builder headers(@NotNull Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(@NotNull String name, @NotNull String value) {
            this.headers.put(name.toLowerCase(), value);
            return this;
        }

        public Builder queryParameters(@NotNull Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder addQueryParameter(@NotNull String name, @NotNull String value) {
            this.queryParameters.put(name, value);
            return this;
        }

        public Builder pathParameters(@NotNull Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        public Builder addPathParameter(@NotNull String name, @NotNull String value) {
            this.pathParameters.put(name, value);
            return this;
        }

        public Builder body(@Nullable String body) {
            this.body = body;
            return this;
        }

        public Builder contentType(@Nullable String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder clientId(@NotNull String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @NotNull
        public HttpRequest build() {
            if (timestamp == 0) {
                timestamp = System.currentTimeMillis();
            }
            return new HttpRequest(this);
        }
    }
}
