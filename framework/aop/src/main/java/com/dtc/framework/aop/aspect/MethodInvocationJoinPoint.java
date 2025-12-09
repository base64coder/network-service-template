package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.aspects.JoinPoint;
import com.dtc.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 方法调用连接点实现
 * 
 * @author Network Service Template
 */
public class MethodInvocationJoinPoint implements JoinPoint {
    
    private final MethodInvocation invocation;
    
    public MethodInvocationJoinPoint(@NotNull MethodInvocation invocation) {
        this.invocation = invocation;
    }
    
    @Override
    @NotNull
    public Object getTarget() {
        return invocation.getThis();
    }
    
    @Override
    @NotNull
    public Object getThis() {
        return invocation.getThis();
    }
    
    @Override
    @NotNull
    public Method getSignature() {
        return invocation.getMethod();
    }
    
    @Override
    @NotNull
    public Object[] getArgs() {
        return invocation.getArgs();
    }
    
    @Override
    @NotNull
    public String getMethodName() {
        return invocation.getMethod().getName();
    }
    
    @Override
    @NotNull
    public Class<?> getTargetClass() {
        return invocation.getThis().getClass();
    }
}

