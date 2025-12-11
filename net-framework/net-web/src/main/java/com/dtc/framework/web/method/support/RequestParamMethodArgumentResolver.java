package com.dtc.framework.web.method.support;

import com.dtc.framework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequestParam ann = parameter.getAnnotation(RequestParam.class);
        String name = ann.value();
        if (name.isEmpty()) {
            name = parameter.getName();
        }
        
        String value = request.getParameter(name);
        if (value == null) {
            if (!ann.defaultValue().isEmpty() && !"\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n".equals(ann.defaultValue())) { // Check default value constant if any
                 value = ann.defaultValue();
            } else if (ann.required()) {
                 throw new IllegalArgumentException("Missing required parameter: " + name);
            }
        }
        
        if (value == null) {
            // Handle primitives by returning defaults
            if (parameter.getType().isPrimitive()) {
                if (parameter.getType() == boolean.class) return false;
                return 0;
            }
            return null;
        }
        
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

