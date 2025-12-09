package com.dtc.annotations.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 返回后通知注解
 * 在目标方法正常返回后执行
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterReturning {
    
    /**
     * 切点表达式
     * 
     * @return 切点表达式
     */
    String value();
    
    /**
     * 返回值参数名
     * 用于在通知方法中获取返回值
     * 
     * @return 返回值参数名
     */
    String returning() default "";
}
