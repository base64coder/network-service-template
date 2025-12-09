package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * ä¸å¯¹å¤å³èå³ç³»æ³¨è§£
æ è¯å®ä½ç±»ä¹é´çä¸å¯¹å¤å³ç³»
åé´MyBatis-Flexç@RelationOneToManyåHibernateç@OneToMany
@author Network Service Template
/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OneToMany {
    
    /**
     * å½åå®ä½ç±»çå³èå­æ®µ
å¦æä¸ºç©ºï¼åä½¿ç¨å½åå®ä½ç±»çä¸»é®
@return å­æ®µå
/
    String selfField() default "";
    
    /**
     * ç®æ å®ä½ç±»çå³èå­æ®µï¼å¤é®ï¼
@return å­æ®µå
/
    String targetField();
    
    /**
     * ç®æ å®ä½ç±»å¯¹åºçè¡¨å
å¦æç®æ å®ä½ç±»ä½¿ç¨äº@Tableæ³¨è§£ï¼å¯ä»¥çç¥
@return è¡¨å
/
    String targetTable() default "";
    
    /**
     * æ¯å¦ç«å³å è½½ï¼EAGERï¼è¿æ¯å»¶è¿å è½½ï¼LAZYï¼
@return å è½½ç­ç¥
/
    FetchType fetch() default FetchType.LAZY;
    
    /**
     * çº§èæä½ç±»å
@return çº§èç±»åæ°ç»
/
    CascadeType[] cascade() default {};
    
    /**
     * æ¥è¯¢æåº
ä¾å¦ï¼"id DESC" æ "createTime ASC"
@return æåºSQL
/
    String orderBy() default "";
    
    /**
     * æ¥è¯¢æ¶çé¢å¤æ¡ä»¶
@return SQLæ¡ä»¶
/
    String extraCondition() default "";
    
    /**
     * å½æ å°ä¸ºMapæ¶ï¼ä½¿ç¨åªä¸ªå­æ®µä½ä¸ºKey
@return å­æ®µå
/
    String mapKeyField() default "";
}

