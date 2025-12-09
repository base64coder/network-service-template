package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Around 通知拦截器
 * 
 * @author Network Service Template
 */
public class AroundMethodInterceptor implements MethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AroundMethodInterceptor.class);
    
    private final Object aspectInstance;
    private final Method adviceMethod;
    
    public AroundMethodInterceptor(@NotNull Object aspectInstance, @NotNull Method adviceMethod) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.adviceMethod.setAccessible(true);
    }
    
    @Override
    @Nullable
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        // 创建 ProceedingJoinPoint
        ProceedingJoinPointImpl proceedingJoinPoint = new ProceedingJoinPointImpl(invocation);
        
        // 执行环绕通知
        try {
            Object[] args = resolveAdviceArguments(proceedingJoinPoint);
            return adviceMethod.invoke(aspectInstance, args);
        } catch (Exception e) {
            log.error("Error executing @Around advice", e);
            throw e;
        }
    }
    
    /**
     * 解析通知方法参数
     */
    @NotNull
    private Object[] resolveAdviceArguments(@NotNull ProceedingJoinPointImpl proceedingJoinPoint) {
        Class<?>[] paramTypes = adviceMethod.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            
            // 支持 ProceedingJoinPoint 参数
            if (com.dtc.framework.aop.aspects.ProceedingJoinPoint.class.isAssignableFrom(paramType)) {
                args[i] = proceedingJoinPoint;
            } else if (com.dtc.framework.aop.aspects.JoinPoint.class.isAssignableFrom(paramType)) {
                args[i] = proceedingJoinPoint;
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
}

