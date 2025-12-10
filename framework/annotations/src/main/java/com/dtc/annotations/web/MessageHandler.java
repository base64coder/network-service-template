package com.dtc.annotations.web;

import com.dtc.annotations.ioc.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息处理器注解
 * 标识一个类为消息处理器，用于处理各种协议的消息
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MessageHandler {
    
    /**
     * 协议类型
     * 支持：UDP, TCP, WebSocket, MQTT, Custom
     * @return 协议类型
     */
    String protocol() default "";
    
    /**
     * 处理器名称
     * @return 名称
     */
    String value() default "";
}
