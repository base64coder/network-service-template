package com.dtc.framework.aop.framework.adapter;

import com.dtc.framework.aop.AfterReturningAdvice;
import com.dtc.framework.aop.MethodInterceptor;
import com.dtc.framework.aop.MethodInvocation;

public class AfterReturningAdviceInterceptor implements MethodInterceptor {
    private final AfterReturningAdvice advice;

    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object ret = mi.proceed();
        advice.afterReturning(ret, mi.getMethod(), mi.getArguments(), mi.getThis());
        return ret;
    }
}

