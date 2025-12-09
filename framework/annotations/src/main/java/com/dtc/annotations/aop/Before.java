package com.dtc.annotations.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前置通知注解
 * 在目标方法执行前执行
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {
    
    /**
     * 切点表达式
     * 例如：execution(* com.dtc.service.*.*(..))
     * 
     * @return 切点表达式
     */
    String value();
}
