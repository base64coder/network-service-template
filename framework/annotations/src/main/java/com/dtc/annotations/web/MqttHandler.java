package com.dtc.annotations.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MQTT消息处理注解
 * 标识一个方法用于处理MQTT消息
 * 
 * @author Network Service Template
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttHandler {
    
    /**
     * 消息类型
     * 支持：CONNECT, PUBLISH, SUBSCRIBE, UNSUBSCRIBE, PING, DISCONNECT
     * 如果为空，则处理所有类型的消息
     * @return 消息类型
     */
    String messageType() default "";
    
    /**
     * 主题匹配模式（用于PUBLISH消息）
     * 支持通配符：+（单级通配符）、#（多级通配符）
     * @return 主题模式
     */
    String topic() default "";
    
    /**
     * 优先级
     * 数值越小，优先级越高
     * @return 优先级
     */
    int priority() default 0;
}
