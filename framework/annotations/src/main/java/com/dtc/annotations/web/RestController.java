package com.dtc.annotations.web;

import com.dtc.annotations.ioc.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * REST控制器注解
 * 标识一个类为REST控制器，自动注册为组件
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestController {
    
    /**
     * 控制器的基础路径
     * @return 基础路径
     */
    String value() default "";
}
