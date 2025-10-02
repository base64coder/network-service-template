package com.dtc.core.bootstrap.ioc.lazysingleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 懒加载单例注解
 * 标记需要懒加载的单例类
 * 
 * @author Network Service Template
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LazySingleton {
}
