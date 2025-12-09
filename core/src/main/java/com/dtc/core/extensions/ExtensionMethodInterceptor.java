package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 扩展方法拦截器
 * 用于拦截和增强扩展实例的方法调用
 */
public class ExtensionMethodInterceptor {
    private final Class<?> extensionClass;
    private final String extensionId;
    private static final Logger log = LoggerFactory.getLogger(ExtensionMethodInterceptor.class);

    public ExtensionMethodInterceptor(Class<?> extensionClass, String extensionId) {
        this.extensionClass = extensionClass;
        this.extensionId = extensionId;
    }

    @RuntimeType
    public Object intercept(@AllArguments Object[] args, @Origin Method method) {
        try {
            String methodName = method.getName();
            log.debug("Intercepting method call: {} on extension: {}", methodName, extensionClass.getName());

            // 处理构造函数调用，不进行拦截
            if ("<init>".equals(methodName)) {
                return null;
            }

            // 处理常用方法，返回默认值
            if ("getId".equals(methodName)) {
                return extensionId != null ? extensionId
                        : extensionClass.getSimpleName().toLowerCase().replace("extension", "");
            }

            if ("getVersion".equals(methodName)) {
                return "1.0.0";
            }

            if ("getExtensionClassloader".equals(methodName)) {
                return Thread.currentThread().getContextClassLoader();
            }

            // 处理其他方法，返回默认值
            return getDefaultReturnValue(method.getReturnType());

        } catch (Exception e) {
            log.error("Error in extension method interceptor", e);
            throw new RuntimeException("Failed to intercept extension method", e);
        }
    }

    @Nullable
    private Object getDefaultReturnValue(@NotNull Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        } else if (returnType == int.class || returnType == Integer.class) {
            return 0;
        } else if (returnType == long.class || returnType == Long.class) {
            return 0L;
        } else if (returnType == double.class || returnType == Double.class) {
            return 0.0;
        } else if (returnType == float.class || returnType == Float.class) {
            return 0.0f;
        } else if (returnType == String.class) {
            return "";
        } else {
            return null;
        }
    }
}
