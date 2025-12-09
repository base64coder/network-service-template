package com.dtc.core.web.argument;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.web.HandlerMethodArgumentResolver;
import com.dtc.annotations.web.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;

/**
 * 请求参数解析器
 * 解析@RequestParam注解的参数
 * 参考Spring的RequestParamMethodArgumentResolver实现
 * 
 * @author Network Service Template
 */
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(RequestParamMethodArgumentResolver.class);

    @Override
    public boolean supportsParameter(@NotNull Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NotNull Parameter parameter, @NotNull HttpRequestEx request) throws Exception {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam == null) {
            return null;
        }

        String paramName = requestParam.value().isEmpty() ? parameter.getName() : requestParam.value();
        String paramValue = request.getQueryParameters().get(paramName);

        if (paramValue == null || paramValue.isEmpty()) {
            if (requestParam.required()) {
                throw new IllegalArgumentException("Required request parameter '" + paramName + "' is missing");
            }
            // 使用默认值
            String defaultValue = requestParam.defaultValue();
            if (!defaultValue.isEmpty()) {
                paramValue = defaultValue;
            } else {
                return null;
            }
        }

        // 类型转换
        return convertValue(paramValue, parameter.getType());
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
            log.warn("Unsupported request parameter type: {}, returning as String", targetType.getName());
            return value;
        }
    }
}
