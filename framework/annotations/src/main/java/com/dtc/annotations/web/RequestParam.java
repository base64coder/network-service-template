package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求参数注解
 * 用于绑定HTTP请求参数
 * 
 * @author Network Service Template
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    
    /**
     * 参数名称
     * 如果为空，则使用参数名称
     * @return 参数名称
     */
    String value() default "";
    
    /**
     * 是否必需
     * @return 是否必需
     */
    boolean required() default true;
    
    /**
     * 默认值
     * @return 默认值
     */
    String defaultValue() default "";
}
