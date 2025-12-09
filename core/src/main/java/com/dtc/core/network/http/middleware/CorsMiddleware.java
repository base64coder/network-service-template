package com.dtc.core.network.http.middleware;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * CORS æ¶î¢æ£¿æµ ?æ¾¶å­æçºã¥çç§å¬ç°®éåé©
 * 
 * @author Network Service Template
 */
public class CorsMiddleware implements HttpMiddleware {

    private final String allowedOrigins;
    private final String allowedMethods;
    private final String allowedHeaders;
    private final boolean allowCredentials;

    public CorsMiddleware() {
        this("*", "GET, POST, PUT, DELETE, OPTIONS", "Content-Type, Authorization", false);
    }

    public CorsMiddleware(@NotNull String allowedOrigins, @NotNull String allowedMethods,
            @NotNull String allowedHeaders, boolean allowCredentials) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.allowCredentials = allowCredentials;
    }

    @Override
    @Nullable
    public HttpResponseEx beforeRequest(@NotNull HttpRequestEx request) {
        // æ¾¶å­ææ£°å¬îçéç°
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createPreflightResponse();
        }

        return null;
    }

    @Override
    @Nullable
    public HttpResponseEx afterRequest(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        // å¨£è¯²å§ CORS æ¾¶æ®å´
        return addCorsHeaders(response);
    }

    @Override
    public int getPriority() {
        return 10; // æ¥æ¨¹ç´­éå ¢éª
    }

    /**
     * éæ¶ç¼æ£°å¬îéå¶ç°²
     */
    @NotNull
    private HttpResponseEx createPreflightResponse() {
        return new HttpResponseEx.Builder().statusCode(200).statusMessage("OK")
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders).addHeader("Access-Control-Max-Age", "86400")
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials)).build();
    }

    /**
     * å¨£è¯²å§ CORS æ¾¶æ®å´
     */
    @NotNull
    private HttpResponseEx addCorsHeaders(@NotNull HttpResponseEx response) {
        return new HttpResponseEx.Builder().statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage()).headers(response.getHeaders())
                .addHeader("Access-Control-Allow-Origin", allowedOrigins)
                .addHeader("Access-Control-Allow-Methods", allowedMethods)
                .addHeader("Access-Control-Allow-Headers", allowedHeaders)
                .addHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials))
                .body(response.getBody()).contentType(response.getContentType()).build();
    }
}
