package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;
import java.lang.reflect.Method;

/**
 * 方法签名实现
 * 
 * @author Network Service Template
 */
public class MethodSignatureImpl implements MethodSignature {
    
    private final Method method;
    
    public MethodSignatureImpl(@NotNull Method method) {
        this.method = method;
    }
    
    @Override
    @NotNull
    public Class<?> getDeclaringType() {
        return method.getDeclaringClass();
    }
    
    @Override
    @NotNull
    public String getName() {
        return method.getName();
    }
    
    @Override
    public int getModifiers() {
        return method.getModifiers();
    }
    
    @Override
    @NotNull
    public Method getMethod() {
        return method;
    }
    
    @Override
    @NotNull
    public Class<?> getReturnType() {
        return method.getReturnType();
    }
    
    @Override
    @NotNull
    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }
    
    @Override
    @NotNull
    public String toString() {
        return method.toString();
    }
}

