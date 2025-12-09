package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * GETè¯·æ±æ å°æ³¨è§£
ç¨äºæ å°GETè¯·æ±å°å¤çæ¹æ³
åé´Springç@GetMappingæ³¨è§£
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMapping.RequestMethod.GET)
public @interface GetMapping {
    
    /**
     * è¯·æ±è·¯å¾
@return è·¯å¾
/
    String value() default "";
    
    /**
     * è¯·æ±åæ°æ¡ä»¶
@return åæ°æ¡ä»¶
/
    String[] params() default {};
    
    /**
     * è¯·æ±å¤´æ¡ä»¶
@return è¯·æ±å¤´æ¡ä»¶
/
    String[] headers() default {};
    
    /**
     * åå®¹ç±»åæ¡ä»¶
@return åå®¹ç±»å
/
    String[] consumes() default {};
    
    /**
     * æ¥åçåå®¹ç±»å
@return æ¥åçåå®¹ç±»å
/
    String[] produces() default {};
}

