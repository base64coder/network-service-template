package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.aspects.JoinPoint;
import com.dtc.framework.aop.aspects.MethodSignature;
import com.dtc.framework.aop.aspects.MethodSignatureImpl;
import com.dtc.framework.aop.intercept.MethodInvocation;

/**
 * 方法调用连接点实现
 * 
 * @author Network Service Template
 */
public class MethodInvocationJoinPoint implements JoinPoint {
    
    private final MethodInvocation invocation;
    private final MethodSignature signature;
    
    public MethodInvocationJoinPoint(@NotNull MethodInvocation invocation) {
        this.invocation = invocation;
        this.signature = new MethodSignatureImpl(invocation.getMethod());
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
    public MethodSignature getSignature() {
        return signature;
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

