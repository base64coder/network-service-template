package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GET请求映射注解
 * 用于映射GET请求到处理方法
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMapping.RequestMethod.GET)
public @interface GetMapping {
    
    /**
     * 请求路径
     * @return 路径
     */
    String value() default "";
    
    /**
     * 请求参数条件
     * @return 参数条件
     */
    String[] params() default {};
    
    /**
     * 请求头条件
     * @return 请求头条件
     */
    String[] headers() default {};
    
    /**
     * 内容类型条件
     * @return 内容类型
     */
    String[] consumes() default {};
    
    /**
     * 接受的内容类型
     * @return 接受的内容类型
     */
    String[] produces() default {};
}
