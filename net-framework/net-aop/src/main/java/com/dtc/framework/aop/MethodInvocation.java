package com.dtc.framework.aop;

import java.lang.reflect.Method;

/**
 * 方法调用描述
 */
public interface MethodInvocation {
    Method getMethod();
    Object[] getArguments();
    Object getThis();
    Object proceed() throws Throwable;
}

