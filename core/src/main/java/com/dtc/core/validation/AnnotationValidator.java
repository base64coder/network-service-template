package com.dtc.core.validation;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解验证器
 * 提供基于 @NotNull 和 @Nullable 注解的参数验证
 * 
 * @author Network Service Template
 */
public class AnnotationValidator {

    private static final Logger log = LoggerFactory.getLogger(AnnotationValidator.class);

    /**
     * 验证方法参数
     * 
     * @param method 方法
     * @param args   参数值
     * @throws IllegalArgumentException 如果验证失败
     */
    public static void validateMethodParameters(@NotNull Method method, @NotNull Object[] args) {
        Parameter[] parameters = method.getParameters();

        if (parameters.length != args.length) {
            throw new IllegalArgumentException(
                    String.format("Parameter count mismatch for method %s: expected %d, got %d",
                            method.getName(), parameters.length, args.length));
        }

        List<String> errors = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            // 检查 @NotNull 注解
            if (parameter.isAnnotationPresent(NotNull.class)) {
                if (arg == null) {
                    errors.add(String.format("Parameter '%s' (index %d) of method '%s' cannot be null",
                            parameter.getName(), i, method.getName()));
                }
            }

            // 检查 @Nullable 注解（通常不需要特殊处理，但可以记录）
            if (parameter.isAnnotationPresent(Nullable.class)) {
                log.debug("Parameter '{}' (index {}) of method '{}' is nullable",
                        parameter.getName(), i, method.getName());
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);
            log.error("Parameter validation failed for method {}: {}", method.getName(), errorMessage);
            throw new IllegalArgumentException("Parameter validation failed: " + errorMessage);
        }
    }

    /**
     * 验证方法返回值
     * 
     * @param method      方法
     * @param returnValue 返回值
     * @throws IllegalArgumentException 如果验证失败
     */
    public static void validateMethodReturnValue(@NotNull Method method, @Nullable Object returnValue) {
        // 检查方法返回值的 @NotNull 注解
        if (method.isAnnotationPresent(NotNull.class)) {
            if (returnValue == null) {
                String errorMessage = String.format("Return value of method '%s' cannot be null", method.getName());
                log.error("Return value validation failed: {}", errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * 验证字段值
     * 
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @param annotations 字段上的注解
     * @throws IllegalArgumentException 如果验证失败
     */
    public static void validateFieldValue(@NotNull String fieldName, @Nullable Object fieldValue,
            @NotNull Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof NotNull) {
                if (fieldValue == null) {
                    String errorMessage = String.format("Field '%s' cannot be null", fieldName);
                    log.error("Field validation failed: {}", errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
    }

    /**
     * 检查方法是否有参数验证需求
     * 
     * @param method 方法
     * @return 是否需要验证
     */
    public static boolean needsParameterValidation(@NotNull Method method) {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(NotNull.class) || parameter.isAnnotationPresent(Nullable.class)) {
                return true;
            }
        }
        return method.isAnnotationPresent(NotNull.class);
    }

    /**
     * 获取验证统计信息
     * 
     * @param method 方法
     * @return 验证统计
     */
    @NotNull
    public static ValidationStats getValidationStats(@NotNull Method method) {
        Parameter[] parameters = method.getParameters();
        int notNullParams = 0;
        int nullableParams = 0;
        boolean notNullReturn = method.isAnnotationPresent(NotNull.class);

        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(NotNull.class)) {
                notNullParams++;
            } else if (parameter.isAnnotationPresent(Nullable.class)) {
                nullableParams++;
            }
        }

        return new ValidationStats(notNullParams, nullableParams, notNullReturn);
    }

    /**
     * 验证统计信息
     */
    public static class ValidationStats {
        private final int notNullParameters;
        private final int nullableParameters;
        private final boolean notNullReturn;

        public ValidationStats(int notNullParameters, int nullableParameters, boolean notNullReturn) {
            this.notNullParameters = notNullParameters;
            this.nullableParameters = nullableParameters;
            this.notNullReturn = notNullReturn;
        }

        public int getNotNullParameters() {
            return notNullParameters;
        }

        public int getNullableParameters() {
            return nullableParameters;
        }

        public boolean isNotNullReturn() {
            return notNullReturn;
        }

        @Override
        public String toString() {
            return String.format("ValidationStats{notNullParams=%d, nullableParams=%d, notNullReturn=%s}",
                    notNullParameters, nullableParameters, notNullReturn);
        }
    }
}
