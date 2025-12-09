package com.dtc.core.web.argument;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.web.HandlerMethodArgumentResolver;
import com.dtc.annotations.web.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;

/**
 * 请求体参数解析器
 * 解析@RequestBody注解的参数
 * 参考Spring的RequestBodyMethodArgumentResolver实现
 * 
 * @author Network Service Template
 */
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(RequestBodyMethodArgumentResolver.class);

    private final ObjectMapper objectMapper;

    public RequestBodyMethodArgumentResolver() {
        this.objectMapper = new ObjectMapper();
    }

    public RequestBodyMethodArgumentResolver(@NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsParameter(@NotNull Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NotNull Parameter parameter, @NotNull HttpRequestEx request) throws Exception {
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        if (requestBody == null) {
            return null;
        }

        String body = request.getBody();
        if (body == null || body.isEmpty()) {
            if (requestBody.required()) {
                throw new IllegalArgumentException("Required request body is missing");
            }
            return null;
        }

        try {
            // 使用Jackson解析JSON
            return objectMapper.readValue(body, parameter.getType());
        } catch (Exception e) {
            log.error("Failed to parse request body as {}", parameter.getType().getName(), e);
            throw new IllegalArgumentException("Failed to parse request body: " + e.getMessage(), e);
        }
    }
}
