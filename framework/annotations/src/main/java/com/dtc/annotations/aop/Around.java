package com.dtc.annotations.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 环绕通知注解
 * 在目标方法执行前后都可以执行逻辑
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Around {
    
    /**
     * 切点表达式
     * 
     * @return 切点表达式
     */
    String value();
}
