package com.dtc.annotations.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * æå¡æ³¨è§£
æ è¯ä¸ä¸ªç±»ä¸ºæå¡å±ç»ä»¶
åé´Springç@Serviceæ³¨è§£
<p>è¡¨ç¤ºä¸ä¸ªç±»æ¯"æå¡"ï¼å¨é¢åé©±å¨è®¾è®¡ï¼DDDï¼ä¸­å®ä¹ä¸º"ä½ä¸ºæ¥å£æä¾çæä½ï¼
å¨æ¨¡åä¸­ç¬ç«å­å¨ï¼æ²¡æå°è£ç¶æ"ã
<p>æ­¤æ³¨è§£ä½ä¸º{@link Component @Component}çç¹åï¼åè®¸éè¿ç±»è·¯å¾æ«æèªå¨æ£æµå®ç°ç±»ã
@author Network Service Template
/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    
    /**
     * Beanåç§°
å¦æä¸ºç©ºï¼åä½¿ç¨ç±»åï¼é¦å­æ¯å°åï¼
@return Beanåç§°
/
    String value() default "";
}

