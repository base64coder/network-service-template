package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * è¯·æ±ä½æ³¨è§£
ç¨äºç»å®HTTPè¯·æ±ä½
åé´Springç@RequestBodyæ³¨è§£
@author Network Service Template
/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
    
    /**
     * æ¯å¦å¿é
@return æ¯å¦å¿é
/
    boolean required() default true;
}

