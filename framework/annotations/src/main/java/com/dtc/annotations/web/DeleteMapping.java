package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * DELETEè¯·æ±æ å°æ³¨è§£
ç¨äºæ å°DELETEè¯·æ±å°å¤çæ¹æ³
åé´Springç@DeleteMappingæ³¨è§£
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMapping.RequestMethod.DELETE)
public @interface DeleteMapping {
    
    /**
     * è¯·æ±è·¯å¾
@return è·¯å¾
/
    String value() default "";
}

