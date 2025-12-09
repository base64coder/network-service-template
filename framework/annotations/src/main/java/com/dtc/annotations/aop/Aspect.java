package com.dtc.annotations.aop;

import com.dtc.annotations.ioc.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 切面注解
 * 标识一个类为切面类
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Aspect {
    
    /**
     * 切面名称
     * 
     * @return 切面名称
     */
    String value() default "";
    
    /**
     * 切面优先级
     * 数值越小，优先级越高
     * 
     * @return 优先级
     */
    int order() default 0;
}
