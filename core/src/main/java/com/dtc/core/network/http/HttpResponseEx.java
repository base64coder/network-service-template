package com.dtc.core.network.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP éå¶ç°²å¦¯â³ç· çä½½î HTTP éå¶ç°²é¨å¬å¢éå¤ä¿é­? * æµ£è·¨æ¤ HttpResponseEx é¬å®å¤æ¶åº¡å¾æµ æ §ç°±é¨?HttpResponse ç»«è¯²æéè¬ç
 * 
 * @author Network Service Template
 */
public class HttpResponseEx {

    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers;
    private final String body;
    private final String contentType;
    private final long timestamp;

    private HttpResponseEx(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.headers = new ConcurrentHashMap<>(builder.headers);
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.timestamp = builder.timestamp;
    }

    // Getters
    public int getStatusCode() {
        return statusCode;
    }

    @NotNull
    public String getStatusMessage() {
        return statusMessage;
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    @Nullable
    public String getContentType() {
        return contentType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // æ¸æåµéè§ç¡¶
    @Nullable
    public String getHeader(@NotNull String name) {
        return headers.get(name.toLowerCase());
    }

    public boolean hasHeader(@NotNull String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    public boolean isJsonContent() {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    public boolean isTextContent() {
        return contentType != null && contentType.toLowerCase().contains("text/");
    }

    public boolean isHtmlContent() {
        return contentType != null && contentType.toLowerCase().contains("text/html");
    }

    @Override
    public String toString() {
        return String.format("HttpResponseEx{statusCode=%d, statusMessage='%s', contentType='%s', timestamp=%d}",
                statusCode, statusMessage, contentType, timestamp);
    }

    /**
     * éå«ç¼é£
     */
    public static class Builder {
        private int statusCode = 200;
        private String statusMessage = "OK";
        private Map<String, String> headers = new ConcurrentHashMap<>();
        private String body;
        private String contentType;
        private long timestamp;

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusMessage(@NotNull String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder status(@NotNull HttpResponseStatus status) {
            this.statusCode = status.code();
            this.statusMessage = status.reasonPhrase();
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

        public Builder body(@Nullable String body) {
            this.body = body;
            return this;
        }

        public Builder contentType(@Nullable String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        // æ¸æåµéè§ç¡¶
        public Builder ok() {
            return statusCode(200).statusMessage("OK");
        }

        public Builder created() {
            return statusCode(201).statusMessage("Created");
        }

        public Builder noContent() {
            return statusCode(204).statusMessage("No Content");
        }

        public Builder badRequest() {
            return statusCode(400).statusMessage("Bad Request");
        }

        public Builder unauthorized() {
            return statusCode(401).statusMessage("Unauthorized");
        }

        public Builder forbidden() {
            return statusCode(403).statusMessage("Forbidden");
        }

        public Builder notFound() {
            return statusCode(404).statusMessage("Not Found");
        }

        public Builder methodNotAllowed() {
            return statusCode(405).statusMessage("Method Not Allowed");
        }

        public Builder conflict() {
            return statusCode(409).statusMessage("Conflict");
        }

        public Builder internalServerError() {
            return statusCode(500).statusMessage("Internal Server Error");
        }

        public Builder serviceUnavailable() {
            return statusCode(503).statusMessage("Service Unavailable");
        }

        public Builder jsonContent() {
            return contentType("application/json; charset=utf-8");
        }

        public Builder textContent() {
            return contentType("text/plain; charset=utf-8");
        }

        public Builder htmlContent() {
            return contentType("text/html; charset=utf-8");
        }

        public Builder xmlContent() {
            return contentType("application/xml; charset=utf-8");
        }

        @NotNull
        public HttpResponseEx build() {
            if (timestamp == 0) {
                timestamp = System.currentTimeMillis();
            }
            return new HttpResponseEx(this);
        }
    }
}