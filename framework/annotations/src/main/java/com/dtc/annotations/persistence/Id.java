package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * ä¸»é®æ³¨è§£
æ è¯å®ä½ç±»çä¸»é®å­æ®µ
åé´MyBatis-Flexç@Idæ³¨è§£
@author Network Service Template
/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Id {
    
    /**
     * ä¸»é®çæç­ç¥
@return çæç­ç¥
/
    KeyType keyType() default KeyType.AUTO;
    
    /**
     * ä¸»é®çæå¨åç§°ï¼å½keyTypeä¸ºGENERATORæ¶ä½¿ç¨ï¼
@return çæå¨åç§°
/
    String generator() default "";
    
    /**
     * ä¸»é®çæç­ç¥æä¸¾
/
    enum KeyType {
        /**
     * æ°æ®åºèªå¢ï¼AUTO_INCREMENTï¼
/
        AUTO,
        
        /**
     * æå¨èµå¼
/
        NONE,
        
        /**
     * UUIDçæ
/
        UUID,
        
        /**
     * éªè±ç®æ³IDçæ
/
        SNOWFLAKE,
        
        /**
     * èªå®ä¹çæå¨
/
        GENERATOR
    }
}

