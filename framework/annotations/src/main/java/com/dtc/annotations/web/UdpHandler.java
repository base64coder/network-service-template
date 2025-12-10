package com.dtc.annotations.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UDP消息处理注解
 * 标识一个方法用于处理UDP消息
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UdpHandler {
    
    /**
     * 消息路由/匹配模式
     * 支持：
     * - 精确匹配：如 "ping"
     * - 前缀匹配：如 "cmd:"
     * - 正则匹配：如 "^ping."
     * 如果为空，则匹配所有消息
     * @return 路由模式
     */
    String value() default "";
    
    /**
     * 优先级
     * 数值越小，优先级越高
     * @return 优先级
     */
    int priority() default 0;
}
