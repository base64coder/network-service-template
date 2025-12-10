package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路径变量注解
 * 用于绑定URL路径中的变量
 * 
 * @author Network Service Template
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    
    /**
     * 路径变量名称
     * 如果为空，则使用参数名称
     * @return 变量名称
     */
    String value() default "";
    
    /**
     * 是否必需
     * @return 是否必需
     */
    boolean required() default true;
}
