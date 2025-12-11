package com.dtc.framework.web.servlet.mvc.method.annotation;

import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.beans.annotation.PostConstruct;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.web.bind.annotation.GetMapping;
import com.dtc.framework.web.bind.annotation.PostMapping;
import com.dtc.framework.web.bind.annotation.RequestMapping;
import com.dtc.framework.web.bind.annotation.RestController;
import com.dtc.framework.web.servlet.HandlerMapping;
import com.dtc.framework.web.util.AntPathMatcher;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping implements HandlerMapping {
    private final Map<String, HandlerMethod> handlerMap = new HashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Inject
    private ApplicationContext context;

    @PostConstruct
    public void initHandlerMethods() {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> beanType = bean.getClass();
            if (beanType.isAnnotationPresent(RestController.class)) {
                String typeMapping = "";
                if (beanType.isAnnotationPresent(RequestMapping.class)) {
                    String[] values = beanType.getAnnotation(RequestMapping.class).value();
                    if (values.length > 0) typeMapping = values[0];
                }
                
                for (Method method : beanType.getDeclaredMethods()) {
                    String path = null;
                    
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        String[] values = method.getAnnotation(RequestMapping.class).value();
                        path = typeMapping + (values.length > 0 ? values[0] : "");
                    } else if (method.isAnnotationPresent(GetMapping.class)) {
                        String[] values = method.getAnnotation(GetMapping.class).value();
                        path = typeMapping + (values.length > 0 ? values[0] : "");
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        String[] values = method.getAnnotation(PostMapping.class).value();
                        path = typeMapping + (values.length > 0 ? values[0] : "");
                    }
                    
                    if (path != null) {
                        handlerMap.put(path, new HandlerMethod(bean, method));
                    }
                }
            }
        }
    }

    @Override
    public Object getHandler(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        
        // 1. Exact match
        HandlerMethod handler = handlerMap.get(uri);
        if (handler != null) {
            return handler;
        }
        
        // 2. Pattern match
        for (Map.Entry<String, HandlerMethod> entry : handlerMap.entrySet()) {
            String pattern = entry.getKey();
            if (pathMatcher.match(pattern, uri)) {
                Map<String, String> variables = pathMatcher.extractUriTemplateVariables(pattern, uri);
                request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    public static class HandlerMethod {
        private final Object bean;
        private final Method method;
        public HandlerMethod(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
        public Object getBean() { return bean; }
        public Method getMethod() { return method; }
    }
}

