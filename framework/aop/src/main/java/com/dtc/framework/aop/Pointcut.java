package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

/**
     * AOP åç¹æ¥å£
å®ä¹å¨åªäºè¿æ¥ç¹åºç¨éç¥
@author Network Service Template
/
public interface Pointcut {

    /**
     * è·åç±»è¿æ»¤å¨
@return ç±»è¿æ»¤å¨
/
    @NotNull
    ClassFilter getClassFilter();

    /**
     * è·åæ¹æ³å¹éå¨
@return æ¹æ³å¹éå¨
/
    @NotNull
    MethodMatcher getMethodMatcher();

    /**
     * å§ç»å¹éçåç¹å®ä¾
/
    Pointcut TRUE = support.TruePointcut.INSTANCE;
}

