package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * 可执行的连接点接口
 * 用于环绕通知，可以控制方法执行
 * 
 * @author Network Service Template
 */
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * 继续执行目标方法
     * @return 方法返回值
     * @throws Throwable 如果方法执行抛出异常
     */
    @Nullable
    Object proceed() throws Throwable;

    /**
     * 使用指定参数继续执行目标方法
     * @param args 方法参数
     * @return 方法返回值
     * @throws Throwable 如果方法执行抛出异常
     */
    @Nullable
    Object proceed(@NotNull Object[] args) throws Throwable;
}
