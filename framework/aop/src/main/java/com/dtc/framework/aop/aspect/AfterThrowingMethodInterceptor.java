package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @AfterThrowing 通知拦截器
 * 
 * @author Network Service Template
 */
public class AfterThrowingMethodInterceptor implements MethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AfterThrowingMethodInterceptor.class);
    
    private final Object aspectInstance;
    private final Method adviceMethod;
    private final String throwingParameterName;
    
    public AfterThrowingMethodInterceptor(@NotNull Object aspectInstance, @NotNull Method adviceMethod, 
                                         @NotNull String throwingParameterName) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.throwingParameterName = throwingParameterName;
        this.adviceMethod.setAccessible(true);
    }
    
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        try {
            // 执行目标方法
            return invocation.proceed();
        } catch (Throwable e) {
            // 执行异常后通知
            try {
                Object[] args = resolveAdviceArguments(invocation, e);
                adviceMethod.invoke(aspectInstance, args);
            } catch (Exception ex) {
                log.error("Error executing @AfterThrowing advice", ex);
            }
            throw e; // 重新抛出异常
        }
    }
    
    /**
     * 解析通知方法参数
     */
    @NotNull
    private Object[] resolveAdviceArguments(@NotNull MethodInvocation invocation, @NotNull Throwable exception) {
        Class<?>[] paramTypes = adviceMethod.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = adviceMethod.getParameters();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            String paramName = parameters[i].getName();
            
            // 支持异常参数
            if (throwingParameterName.equals(paramName) || "throwing".equals(paramName)) {
                args[i] = exception;
            } else if (Throwable.class.isAssignableFrom(paramType) && exception.getClass().isAssignableFrom(paramType)) {
                args[i] = exception;
            } else if (com.dtc.framework.aop.aspects.JoinPoint.class.isAssignableFrom(paramType)) {
                args[i] = new MethodInvocationJoinPoint(invocation);
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
}

