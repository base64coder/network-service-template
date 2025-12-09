package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * ä¸å¯¹ä¸å³èå³ç³»æ³¨è§£
æ è¯å®ä½ç±»ä¹é´çä¸å¯¹ä¸å³ç³»
åé´MyBatis-Flexç@RelationOneToOneåHibernateç@OneToOne
@author Network Service Template
/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OneToOne {
    
    /**
     * å½åå®ä½ç±»çå³èå­æ®µ
å¦æä¸ºç©ºï¼åä½¿ç¨å½åå­æ®µåå¯¹åºçä¸åçº¿å­æ®µå
@return å­æ®µå
/
    String selfField() default "";
    
    /**
     * ç®æ å®ä½ç±»çå³èå­æ®µ
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
     * æ¯å¦å¯é
å¦æä¸ºfalseï¼å³èå¿é¡»å­å¨
@return æ¯å¦å¯é
/
    boolean optional() default true;
    
    /**
     * ä¸­é´è¡¨åç§°ï¼ç¨äºéè¿ä¸­é´è¡¨ç»´æ¤ä¸å¯¹ä¸å³ç³»ï¼
@return ä¸­é´è¡¨å
/
    String joinTable() default "";
    
    /**
     * ä¸­é´è¡¨ä¸å½åè¡¨çå³èå­æ®µ
@return å­æ®µå
/
    String joinSelfColumn() default "";
    
    /**
     * ä¸­é´è¡¨ä¸ç®æ è¡¨çå³èå­æ®µ
@return å­æ®µå
/
    String joinTargetColumn() default "";
    
    /**
     * æ¥è¯¢æ¶çé¢å¤æ¡ä»¶
@return SQLæ¡ä»¶
/
    String extraCondition() default "";
}

