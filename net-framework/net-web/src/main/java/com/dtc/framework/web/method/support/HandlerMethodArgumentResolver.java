package com.dtc.framework.web.method.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public interface HandlerMethodArgumentResolver {
    
    boolean supportsParameter(Parameter parameter);
    
    Object resolveArgument(Parameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception;
}

