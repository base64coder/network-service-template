package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @AfterReturning 通知拦截器
 * 
 * @author Network Service Template
 */
public class AfterReturningMethodInterceptor implements MethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AfterReturningMethodInterceptor.class);
    
    private final Object aspectInstance;
    private final Method adviceMethod;
    private final String returningParameterName;
    
    public AfterReturningMethodInterceptor(@NotNull Object aspectInstance, @NotNull Method adviceMethod, 
                                          @NotNull String returningParameterName) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.returningParameterName = returningParameterName;
        this.adviceMethod.setAccessible(true);
    }
    
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        // 执行目标方法
        Object result = invocation.proceed();
        
        // 执行返回后通知
        try {
            Object[] args = resolveAdviceArguments(invocation, result);
            adviceMethod.invoke(aspectInstance, args);
        } catch (Exception e) {
            log.error("Error executing @AfterReturning advice", e);
        }
        
        return result;
    }
    
    /**
     * 解析通知方法参数
     */
    @NotNull
    private Object[] resolveAdviceArguments(@NotNull MethodInvocation invocation, @NotNull Object result) {
        Class<?>[] paramTypes = adviceMethod.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = adviceMethod.getParameters();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            String paramName = parameters[i].getName();
            
            // 支持返回值参数
            if (returningParameterName.equals(paramName) || "returning".equals(paramName)) {
                args[i] = result;
            } else if (com.dtc.framework.aop.aspects.JoinPoint.class.isAssignableFrom(paramType)) {
                args[i] = new MethodInvocationJoinPoint(invocation);
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
}

