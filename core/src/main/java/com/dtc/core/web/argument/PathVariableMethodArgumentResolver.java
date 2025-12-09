package com.dtc.core.web.argument;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.web.HandlerMethodArgumentResolver;
import com.dtc.annotations.web.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;

/**
 * 路径变量参数解析器
 * 解析@PathVariable注解的参数
 * 参考Spring的PathVariableMethodArgumentResolver实现
 * 
 * @author Network Service Template
 */
public class PathVariableMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(PathVariableMethodArgumentResolver.class);

    @Override
    public boolean supportsParameter(@NotNull Parameter parameter) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NotNull Parameter parameter, @NotNull HttpRequestEx request) throws Exception {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable == null) {
            return null;
        }

        String paramName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();
        String pathValue = request.getPathParameters().get(paramName);

        if (pathValue == null) {
            if (pathVariable.required()) {
                throw new IllegalArgumentException("Required path variable '" + paramName + "' is missing");
            }
            return null;
        }

        // 类型转换
        return convertValue(pathValue, parameter.getType());
    }

    /**
     * 类型转换处理
     */
    @Nullable
    private Object convertValue(@NotNull String value, @NotNull Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            log.warn("Unsupported path variable type: {}, returning as String", targetType.getName());
            return value;
        }
    }
}
