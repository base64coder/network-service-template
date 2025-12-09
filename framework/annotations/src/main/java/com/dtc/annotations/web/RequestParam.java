package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * è¯·æ±åæ°æ³¨è§£
ç¨äºç»å®HTTPè¯·æ±åæ°
åé´Springç@RequestParamæ³¨è§£
@author Network Service Template
/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    
    /**
     * åæ°åç§°
å¦æä¸ºç©ºï¼åä½¿ç¨åæ°åç§°
@return åæ°åç§°
/
    String value() default "";
    
    /**
     * æ¯å¦å¿é
@return æ¯å¦å¿é
/
    boolean required() default true;
    
    /**
     * é»è®¤å¼
@return é»è®¤å¼
/
    String defaultValue() default "";
}

