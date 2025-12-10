package com.dtc.core.validation;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 验证拦截器
 * 通过 AOP 拦截方法调用并执行参数和返回值的验证
 * 
 * @author Network Service Template
 */
public class ValidationInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ValidationInterceptor.class);

    @Override
    @Nullable
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();

        // 记录方法调用
        log.debug("Intercepting method call: {}.{}",
                method.getDeclaringClass().getSimpleName(), method.getName());

        try {
            // 验证参数
            if (AnnotationValidator.needsParameterValidation(method)) {
                AnnotationValidator.validateMethodParameters(method, arguments);
                log.debug("Parameter validation passed for method: {}", method.getName());
            }

            // 执行目标方法
            Object result = invocation.proceed();

            // 验证返回值
            AnnotationValidator.validateMethodReturnValue(method, result);

            log.debug("Method execution completed successfully: {}", method.getName());
            return result;

        } catch (IllegalArgumentException e) {
            log.error("Validation failed for method {}: {}", method.getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Method execution failed: {}", method.getName(), e);
            throw e;
        }
    }
}
