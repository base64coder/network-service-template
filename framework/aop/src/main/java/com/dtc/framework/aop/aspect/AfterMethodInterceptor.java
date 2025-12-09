package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @After 通知拦截器
 * 
 * @author Network Service Template
 */
public class AfterMethodInterceptor implements MethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AfterMethodInterceptor.class);
    
    private final Object aspectInstance;
    private final Method adviceMethod;
    
    public AfterMethodInterceptor(@NotNull Object aspectInstance, @NotNull Method adviceMethod) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.adviceMethod.setAccessible(true);
    }
    
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        Object result = null;
        Throwable exception = null;
        
        try {
            // 先执行目标方法
            result = invocation.proceed();
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // 无论是否抛出异常，都执行后置通知
            try {
                Object[] args = resolveAdviceArguments(invocation, result, exception);
                adviceMethod.invoke(aspectInstance, args);
            } catch (Exception e) {
                log.error("Error executing @After advice", e);
            }
        }
        
        return result;
    }
    
    /**
     * 解析通知方法参数
     */
    @NotNull
    private Object[] resolveAdviceArguments(@NotNull MethodInvocation invocation, 
                                             Object result, Throwable exception) {
        Class<?>[] paramTypes = adviceMethod.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            
            // 支持 JoinPoint 参数
            if (com.dtc.framework.aop.aspects.JoinPoint.class.isAssignableFrom(paramType)) {
                args[i] = new MethodInvocationJoinPoint(invocation);
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
}

