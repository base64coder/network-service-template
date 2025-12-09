package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * èªå¨è£éæ³¨è§£
æ è¯éè¦èªå¨æ³¨å¥çä¾èµ
åé´Springç@Autowiredæ³¨è§£
@author Network Service Template
/
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    
    /**
     * æ¯å¦å¿é
å¦æä¸ºtrueï¼åä¾èµå¿é¡»å­å¨ï¼å¦åæåºå¼å¸¸
å¦æä¸ºfalseï¼åä¾èµå¯ä»¥ä¸ºnull
@return æ¯å¦å¿é
/
    boolean required() default true;
}

