package com.dtc.ioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动装配注解
 * 标识需要自动注入的依赖
 * 借鉴Spring的@Autowired注解
 * 
 * @author Network Service Template
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    
    /**
     * 是否必需
     * 如果为true，则依赖必须存在，否则抛出异常
     * 如果为false，则依赖可以为null
     * 
     * @return 是否必需
     */
    boolean required() default true;
}
