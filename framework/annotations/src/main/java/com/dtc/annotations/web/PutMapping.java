package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * PUTè¯·æ±æ å°æ³¨è§£
ç¨äºæ å°PUTè¯·æ±å°å¤çæ¹æ³
åé´Springç@PutMappingæ³¨è§£
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMapping.RequestMethod.PUT)
public @interface PutMapping {
    
    /**
     * è¯·æ±è·¯å¾
@return è·¯å¾
/
    String value() default "";
}

