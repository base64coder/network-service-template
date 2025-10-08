package com.dtc.ioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 值注解
 * 用于注入配置值
 * 借鉴Spring的@Value注解
 * 
 * @author Network Service Template
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    
    /**
     * 配置值表达式
     * 支持${property.name}格式
     * 
     * @return 配置值表达式
     */
    String value();
}
