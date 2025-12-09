package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * ç»ä»¶æ³¨è§£
æ è¯ä¸ä¸ªç±»ä¸ºå®¹å¨ç®¡ççç»ä»¶
åé´Springç@Componentæ³¨è§£
@author Network Service Template
/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    
    /**
     * Beanåç§°
å¦æä¸ºç©ºï¼åä½¿ç¨ç±»åï¼é¦å­æ¯å°åï¼
@return Beanåç§°
/
    String value() default "";
}

