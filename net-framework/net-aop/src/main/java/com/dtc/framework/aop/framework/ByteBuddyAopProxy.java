package com.dtc.framework.aop.framework;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

public class ByteBuddyAopProxy implements AopProxy {
    private final AdvisedSupport advised;

    public ByteBuddyAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return getProxy(advised.getTargetSource().getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Class<?> targetClass = advised.getTargetSource().getTargetClass();
        try {
            return new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new AopInterceptor(advised)))
                    .make()
                    .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class AopInterceptor {
        private final AdvisedSupport advised;
        
        public AopInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }
        
        @RuntimeType
        public Object intercept(@AllArguments Object[] args,
                                @Origin Method method,
                                @SuperCall Callable<?> callable) throws Throwable {
            Method targetMethod = method;
            if (method.getDeclaringClass() != advised.getTargetClass()) {
                 try {
                     targetMethod = advised.getTargetClass().getMethod(method.getName(), method.getParameterTypes());
                 } catch (NoSuchMethodException e) {
                     // fallback
                 }
            }
            List<Object> chain = advised.getInterceptorsAndDynamicInterceptionAdvice(targetMethod, advised.getTargetClass());
            if (chain.isEmpty()) {
                return callable.call();
            }
            
            // Delegate to target instance
            Object target = advised.getTargetSource().getTarget();
            
            ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
                    target,
                    method,
                    args,
                    chain
            );
            return invocation.proceed();
        }
    }
}

