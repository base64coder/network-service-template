package com.dtc.core.bootstrap.ioc.lazysingleton;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 延迟单例注解
 * 标记需要延迟初始化的单例类型
 * 
 * @author Network Service Template
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface LazySingleton {
}
