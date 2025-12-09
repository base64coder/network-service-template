package com.dtc.annotations.web;

import com.dtc.annotations.ioc.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * æ¶æ¯å¤çå¨æ³¨è§£
æ è¯ä¸ä¸ªç±»ä¸ºæ¶æ¯å¤çå¨ï¼ç¨äºå¤çåç§åè®®çæ¶æ¯
@author Network Service Template
/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MessageHandler {
    
    /**
     * åè®®ç±»å
æ¯æï¼UDP, TCP, WebSocket, MQTT, Custom
@return åè®®ç±»å
/
    String protocol() default "";
    
    /**
     * å¤çå¨åç§°
@return åç§°
/
    String value() default "";
}

