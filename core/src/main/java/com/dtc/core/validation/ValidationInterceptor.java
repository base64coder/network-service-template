package com.dtc.core.validation;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * æ¥ å²çé·ï¸½åé£? * æµ£è·¨æ¤ AOP é·îå§©é·ï¸½åéè§ç¡¶çåªæ¤éªæ°ç¹çå±½å¼¬éæ¿æ°æ©æ¿æ´éå¥¸çç? * 
 * @author Network Service Template
 */
public class ValidationInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ValidationInterceptor.class);

    @Override
    @Nullable
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();

        // çæ¿ç¶éè§ç¡¶çåªæ¤
        log.debug("Intercepting method call: {}.{}",
                method.getDeclaringClass().getSimpleName(), method.getName());

        try {
            // æ¥ å²çéåæ
            if (AnnotationValidator.needsParameterValidation(method)) {
                AnnotationValidator.validateMethodParameters(method, arguments);
                log.debug("Parameter validation passed for method: {}", method.getName());
            }

            // éµÑîéç¸æå¨?            Object result = invocation.proceed();

            // æ¥ å²çæ©æ¿æ´é?            AnnotationValidator.validateMethodReturnValue(method, result);

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
