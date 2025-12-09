package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * è¯·æ±æ å°æ³¨è§£
ç¨äºæ å°HTTPè¯·æ±å°å¤çæ¹æ³
åé´Springç@RequestMappingæ³¨è§£
@author Network Service Template
/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    
    /**
     * è¯·æ±è·¯å¾
@return è·¯å¾
/
    String value() default "";
    
    /**
     * HTTPæ¹æ³
@return HTTPæ¹æ³æ°ç»
/
    RequestMethod[] method() default {};
    
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
    
    /**
     * HTTPæ¹æ³æä¸¾
/
    enum RequestMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }
}

