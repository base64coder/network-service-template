package com.dtc.annotations.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * MQTTæ¶æ¯å¤çæ³¨è§£
æ è¯ä¸ä¸ªæ¹æ³ç¨äºå¤çMQTTæ¶æ¯
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttHandler {
    
    /**
     * æ¶æ¯ç±»å
æ¯æï¼CONNECT, PUBLISH, SUBSCRIBE, UNSUBSCRIBE, PING, DISCONNECT
å¦æä¸ºç©ºï¼åå¤çææç±»åçæ¶æ¯
@return æ¶æ¯ç±»å
/
    String messageType() default "";
    
    /**
     * ä¸»é¢å¹éæ¨¡å¼ï¼ç¨äºPUBLISHæ¶æ¯ï¼
æ¯æééç¬¦ï¼+ï¼åçº§ééç¬¦ï¼ã#ï¼å¤çº§ééç¬¦ï¼
@return ä¸»é¢æ¨¡å¼
/
    String topic() default "";
    
    /**
     * ä¼åçº§
æ°å¼è¶å°ï¼ä¼åçº§è¶é«
@return ä¼åçº§
/
    int priority() default 0;
}

