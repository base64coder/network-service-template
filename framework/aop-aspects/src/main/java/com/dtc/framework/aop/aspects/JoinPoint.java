package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;

import java.lang.reflect.Method;

/**
     * è¿æ¥ç¹æ¥å£
æä¾å¯¹è¿æ¥ç¹ä¿¡æ¯çè®¿é®
@author Network Service Template
/
public interface JoinPoint {

    /**
     * è·åç®æ å¯¹è±¡
@return ç®æ å¯¹è±¡
/
    @NotNull
    Object getTarget();

    /**
     * è·åä»£çå¯¹è±¡
@return ä»£çå¯¹è±¡
/
    @NotNull
    Object getThis();

    /**
     * è·åæ¹æ³ç­¾å
@return æ¹æ³ç­¾å
/
    @NotNull
    Method getSignature();

    /**
     * è·åæ¹æ³åæ°
@return æ¹æ³åæ°æ°ç»
/
    @NotNull
    Object[] getArgs();

    /**
     * è·åæ¹æ³å
@return æ¹æ³å
/
    @NotNull
    String getMethodName();

    /**
     * è·åç®æ ç±»
@return ç®æ ç±»
/
    @NotNull
    Class<?> getTargetClass();
}

