package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * 方法匹配器接口
 * 用于判断方法是否匹配切点条件
 * 
 * @author Network Service Template
 */
public interface MethodMatcher {

    /**
     * 判断方法是否匹配（静态匹配）
     * 
     * @param method 要检查的方法
     * @param targetClass 目标类
     * @return 如果匹配返回 true，否则返回 false
 */
    boolean matches(@NotNull Method method, @NotNull Class<?> targetClass);

    /**
     * 判断方法是否匹配（运行时匹配）
     * 
     * @param method 要检查的方法
     * @param targetClass 目标类
     * @param args 方法参数
     * @return 如果匹配返回 true，否则返回 false
 */
    default boolean matches(@NotNull Method method, @NotNull Class<?> targetClass, @NotNull Object... args) {
        return matches(method, targetClass);
    }

    /**
     * 是否为运行时匹配
     * 
     * @return 如果是运行时匹配返回 true，否则返回 false
 */
    default boolean isRuntime() {
        return false;
    }

    /**
     * 始终匹配的方法匹配器
 */
    MethodMatcher TRUE = support.TrueMethodMatcher.INSTANCE;
}
