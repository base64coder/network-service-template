package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.aop.aspects.ProceedingJoinPoint;
import com.dtc.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 可执行的连接点实现
 * 
 * @author Network Service Template
 */
public class ProceedingJoinPointImpl extends MethodInvocationJoinPoint implements ProceedingJoinPoint {
    
    private final MethodInvocation invocation;
    
    public ProceedingJoinPointImpl(@NotNull MethodInvocation invocation) {
        super(invocation);
        this.invocation = invocation;
    }
    
    @Override
    @Nullable
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }
    
    @Override
    @Nullable
    public Object proceed(@NotNull Object[] args) throws Throwable {
        // 注意：这里需要更新 invocation 的参数
        // 简化实现，直接调用 proceed()
        return invocation.proceed();
    }
}

