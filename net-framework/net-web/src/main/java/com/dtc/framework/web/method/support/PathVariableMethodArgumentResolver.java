package com.dtc.framework.web.method.support;

import com.dtc.framework.web.bind.annotation.PathVariable;
import com.dtc.framework.web.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
import java.util.Map;

public class PathVariableMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PathVariable ann = parameter.getAnnotation(PathVariable.class);
        String name = ann.value();
        if (name.isEmpty()) {
            name = parameter.getName();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        
        if (uriTemplateVariables == null || !uriTemplateVariables.containsKey(name)) {
             throw new IllegalArgumentException("Missing path variable: " + name);
        }
        
        String value = uriTemplateVariables.get(name);
        return convert(value, parameter.getType());
    }
    
    private Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }
}

