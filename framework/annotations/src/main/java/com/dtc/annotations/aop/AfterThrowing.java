package com.dtc.annotations.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常后通知注解
 * 在目标方法抛出异常后执行
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterThrowing {
    
    /**
     * 切点表达式
     * 
     * @return 切点表达式
     */
    String value();
    
    /**
     * 异常参数名
     * 用于在通知方法中获取异常对象
     * 
     * @return 异常参数名
     */
    String throwing() default "";
}
