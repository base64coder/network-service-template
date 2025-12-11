package com.dtc.framework.aop.framework;

import com.dtc.framework.aop.MethodInterceptor;
import com.dtc.framework.aop.MethodInvocation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectiveMethodInvocation implements MethodInvocation {
    protected final Object target;
    protected final Method method;
    protected final Object[] arguments;
    protected final List<Object> interceptorsAndDynamicMethodMatchers;
    private int currentInterceptorIndex = -1;

    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public Object proceed() throws Throwable {
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        
        if (interceptorOrInterceptionAdvice instanceof MethodInterceptor) {
            return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
        } else {
            return proceed();
        }
    }
}

