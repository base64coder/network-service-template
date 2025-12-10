package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * 连接点接口
 * 提供对连接点信息的访问
 * 
 * @author Network Service Template
 */
public interface JoinPoint {

    /**
     * 获取目标对象
     * @return 目标对象
     */
    @NotNull
    Object getTarget();

    /**
     * 获取代理对象
     * @return 代理对象
     */
    @NotNull
    Object getThis();

    /**
     * 获取签名
     * @return 签名对象
     */
    @NotNull
    Signature getSignature();

    /**
     * 获取方法参数
     * @return 方法参数数组
     */
    @NotNull
    Object[] getArgs();

    /**
     * 获取方法名
     * @return 方法名
     */
    @NotNull
    String getMethodName();

    /**
     * 获取目标类
     * @return 目标类
     */
    @NotNull
    Class<?> getTargetClass();
}
