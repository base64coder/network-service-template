package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 后构造注解
 * 标识 Bean 初始化后调用的方法
 * 借鉴 Spring 的 @PostConstruct 注解
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
