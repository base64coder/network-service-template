package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动装配注解
 * 标识需要自动注入的依赖
 * 借鉴 Spring 的 @Autowired 注解
 * 
 * @author Network Service Template
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    
    /**
     * 是否必需
     * 如果为 true，则依赖必须存在，否则抛出异常
     * 如果为 false，则依赖可以为 null
     * 
     * @return 是否必需
     */
    boolean required() default true;
}
