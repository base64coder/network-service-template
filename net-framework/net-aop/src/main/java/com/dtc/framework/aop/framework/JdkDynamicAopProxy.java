package com.dtc.framework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    private final AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return getProxy(advised.getTargetSource().getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, advised.getTargetSource().getTargetClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, advised.getTargetClass());
        if (chain.isEmpty()) {
            return method.invoke(advised.getTargetSource().getTarget(), args);
        }
        
        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
                advised.getTargetSource().getTarget(),
                method,
                args,
                chain
        );
        return invocation.proceed();
    }
}

