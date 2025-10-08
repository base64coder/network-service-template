package com.dtc.ioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 预销毁注解
 * 标识Bean销毁前调用的方法
 * 借鉴Spring的@PreDestroy注解
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
