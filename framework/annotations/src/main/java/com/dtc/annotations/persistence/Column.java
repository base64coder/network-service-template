package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * æ°æ®åºåæ³¨è§£
æ è¯å®ä½å±æ§å¯¹åºçæ°æ®åºå
åé´MyBatis-Flexç@Columnæ³¨è§£
@author Network Service Template
/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Column {
    
    /**
     * ååç§°
å¦æä¸ºç©ºï¼åä½¿ç¨å±æ§åï¼æ ¹æ®camelToUnderlineè½¬æ¢ï¼
@return åå
/
    String value() default "";
    
    /**
     * æ¯å¦å¿½ç¥è¯¥å­æ®µ
å¦æä¸ºtrueï¼è¯¥å­æ®µä¸ä¼æ å°å°æ°æ®åº
@return æ¯å¦å¿½ç¥
/
    boolean ignore() default false;
    
    /**
     * æ¯å¦ä¸ºä¸»é®
@return æ¯å¦ä¸ºä¸»é®
/
    boolean primaryKey() default false;
    
    /**
     * æ¯å¦ä¸ºé»è¾å é¤å­æ®µ
ä¸å¼ è¡¨ä¸­åªè½æä¸ä¸ªé»è¾å é¤å­æ®µ
@return æ¯å¦ä¸ºé»è¾å é¤å­æ®µ
/
    boolean logicDelete() default false;
    
    /**
     * æ¯å¦ä¸ºä¹è§éå­æ®µ
æ´æ°æ¶ä¼æ£æ¥çæ¬å·ï¼æ´æ°æååçæ¬å·+1
åªè½ç¨äºæ°å¼ç±»åå­æ®µ
@return æ¯å¦ä¸ºä¹è§éå­æ®µ
/
    boolean version() default false;
    
    /**
     * åæ³¨é
@return æ³¨éåå®¹
/
    String comment() default "";
    
    /**
     * æ¯å¦åè®¸ä¸ºç©º
@return æ¯å¦åè®¸ä¸ºç©º
/
    boolean nullable() default true;
    
    /**
     * å­æ®µé¿åº¦ï¼ç¨äºå­ç¬¦ä¸²ç±»åï¼
@return é¿åº¦
/
    int length() default 255;
}

