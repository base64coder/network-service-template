package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求体注解
 * 用于绑定HTTP请求体
 * 
 * @author Network Service Template
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
    
    /**
     * 是否必需
     * @return 是否必需
     */
    boolean required() default true;
}
