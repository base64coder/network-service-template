package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Before 通知拦截器
 * 
 * @author Network Service Template
 */
public class BeforeMethodInterceptor implements MethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(BeforeMethodInterceptor.class);
    
    private final Object aspectInstance;
    private final Method adviceMethod;
    
    public BeforeMethodInterceptor(@NotNull Object aspectInstance, @NotNull Method adviceMethod) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.adviceMethod.setAccessible(true);
    }
    
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        // 执行前置通知
        try {
            Object[] args = resolveAdviceArguments(invocation);
            adviceMethod.invoke(aspectInstance, args);
        } catch (Exception e) {
            log.error("Error executing @Before advice", e);
        }
        
        // 继续执行目标方法
        return invocation.proceed();
    }
    
    /**
     * 解析通知方法参数
     */
    @NotNull
    private Object[] resolveAdviceArguments(@NotNull MethodInvocation invocation) {
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

