package com.dtc.framework.aop;

/**
 * 方法拦截器
 */
public interface MethodInterceptor extends Advice {
    Object invoke(MethodInvocation invocation) throws Throwable;
}

