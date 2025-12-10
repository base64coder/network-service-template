package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PUT请求映射注解
 * 用于映射PUT请求到处理方法
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMapping.RequestMethod.PUT)
public @interface PutMapping {
    
    /**
     * 请求路径
     * @return 路径
     */
    String value() default "";
}
