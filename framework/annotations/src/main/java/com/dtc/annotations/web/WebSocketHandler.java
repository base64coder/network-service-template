package com.dtc.annotations.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * WebSocketæ¶æ¯å¤çæ³¨è§£
æ è¯ä¸ä¸ªæ¹æ³ç¨äºå¤çWebSocketæ¶æ¯
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketHandler {
    
    /**
     * æ¶æ¯è·¯ç±/å¹éæ¨¡å¼
æ¯æï¼
- ç²¾ç¡®å¹éï¼å¦ "ping"
- åç¼å¹éï¼å¦ "cmd:"
- æ­£åå¹éï¼å¦ "^ping."
å¦æä¸ºç©ºï¼åå¹éæææ¶æ¯
@return è·¯ç±æ¨¡å¼
/
    String value() default "";
    
    /**
     * ä¼åçº§
æ°å¼è¶å°ï¼ä¼åçº§è¶é«
@return ä¼åçº§
/
    int priority() default 0;
}

