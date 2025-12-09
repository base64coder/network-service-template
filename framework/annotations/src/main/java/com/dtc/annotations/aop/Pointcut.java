package com.dtc.annotations.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 切点注解
 * 定义可重用的切点表达式
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pointcut {
    
    /**
     * 切点表达式
     * 
     * @return 切点表达式
     */
    String value();
    
    /**
     * 切点名称
     * 
     * @return 切点名称
     */
    String name() default "";
}
