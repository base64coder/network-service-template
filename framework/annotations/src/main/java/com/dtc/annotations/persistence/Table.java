package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * æ°æ®åºè¡¨æ³¨è§£
æ è¯å®ä½ç±»å¯¹åºçæ°æ®åºè¡¨
åé´MyBatis-Flexç@Tableæ³¨è§£
@author Network Service Template
/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    
    /**
     * è¡¨åç§°
@return è¡¨å
/
    String value();
    
    /**
     * æ°æ®åºschemaï¼æ¨¡å¼ï¼
@return schemaåç§°
/
    String schema() default "";
    
    /**
     * æ¯å¦å°é©¼å³°å±æ§è½¬æ¢ä¸ºä¸åçº¿å­æ®µ
é»è®¤ä¸ºtrue
@return æ¯å¦è½¬æ¢
/
    boolean camelToUnderline() default true;
    
    /**
     * è¡¨æ³¨é
@return æ³¨éåå®¹
/
    String comment() default "";
}

