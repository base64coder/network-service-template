package com.dtc.framework.web.method.support;

import com.dtc.framework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    public RequestBodyMethodArgumentResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return objectMapper.readValue(request.getInputStream(), parameter.getType());
    }
}

