package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * å¼æ³¨è§£
ç¨äºæ³¨å¥éç½®å¼
åé´Springç@Valueæ³¨è§£
@author Network Service Template
/
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    
    /**
     * éç½®å¼è¡¨è¾¾å¼
æ¯æ${property.name}æ ¼å¼
@return éç½®å¼è¡¨è¾¾å¼
/
    String value();
}

