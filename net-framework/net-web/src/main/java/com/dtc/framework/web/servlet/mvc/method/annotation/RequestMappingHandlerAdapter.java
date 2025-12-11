package com.dtc.framework.web.servlet.mvc.method.annotation;

import com.dtc.framework.web.bind.annotation.ResponseBody;
import com.dtc.framework.web.bind.annotation.RestController;
import com.dtc.framework.web.servlet.HandlerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

import com.dtc.framework.web.method.support.HandlerMethodArgumentResolverComposite;
import com.dtc.framework.web.method.support.PathVariableMethodArgumentResolver;
import com.dtc.framework.web.method.support.RequestBodyMethodArgumentResolver;
import com.dtc.framework.web.method.support.RequestParamMethodArgumentResolver;
import com.dtc.framework.web.method.support.ServletRequestResponseMethodArgumentResolver;
import java.lang.reflect.Parameter;

public class RequestMappingHandlerAdapter implements HandlerAdapter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();

    public RequestMappingHandlerAdapter() {
        argumentResolvers.addResolver(new RequestParamMethodArgumentResolver());
        argumentResolvers.addResolver(new RequestBodyMethodArgumentResolver(objectMapper));
        argumentResolvers.addResolver(new ServletRequestResponseMethodArgumentResolver());
        argumentResolvers.addResolver(new PathVariableMethodArgumentResolver());
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof RequestMappingHandlerMapping.HandlerMethod;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestMappingHandlerMapping.HandlerMethod hm = (RequestMappingHandlerMapping.HandlerMethod) handler;
        Method method = hm.getMethod();
        
        // Argument Resolution
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (argumentResolvers.supportsParameter(param)) {
                args[i] = argumentResolvers.resolveArgument(param, request, response);
            } else {
                args[i] = null; // Or throw exception?
            }
        }
        
        Object result = method.invoke(hm.getBean(), args);
        
        if (method.isAnnotationPresent(ResponseBody.class) || 
            hm.getBean().getClass().isAnnotationPresent(ResponseBody.class) ||
            hm.getBean().getClass().isAnnotationPresent(RestController.class)) {
            
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), result);
            return null;
        }
        
        return result;
    }
}

