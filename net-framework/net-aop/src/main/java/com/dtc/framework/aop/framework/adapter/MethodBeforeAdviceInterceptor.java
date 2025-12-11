package com.dtc.framework.aop.framework.adapter;

import com.dtc.framework.aop.MethodBeforeAdvice;
import com.dtc.framework.aop.MethodInterceptor;
import com.dtc.framework.aop.MethodInvocation;

public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
    private final MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}

