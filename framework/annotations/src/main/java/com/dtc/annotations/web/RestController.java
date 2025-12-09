package com.dtc.annotations.web;

import com.dtc.annotations.ioc.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * RESTæ§å¶å¨æ³¨è§£
æ è¯ä¸ä¸ªç±»ä¸ºRESTæ§å¶å¨ï¼èªå¨æ³¨åä¸ºç»ä»¶
åé´Springç@RestControlleræ³¨è§£
@author Network Service Template
/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestController {
    
    /**
     * æ§å¶å¨çåºç¡è·¯å¾
@return åºç¡è·¯å¾
/
    String value() default "";
}

