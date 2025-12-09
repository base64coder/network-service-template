package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * å¯æ§è¡çè¿æ¥ç¹æ¥å£
ç¨äºç¯ç»éç¥ï¼å¯ä»¥æ§å¶æ¹æ³æ§è¡
@author Network Service Template
/
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * ç»§ç»­æ§è¡ç®æ æ¹æ³
@return æ¹æ³è¿åå¼
@throws Throwable å¦ææ¹æ³æ§è¡æåºå¼å¸¸
/
    @Nullable
    Object proceed() throws Throwable;

    /**
     * ä½¿ç¨æå®åæ°ç»§ç»­æ§è¡ç®æ æ¹æ³
@param args æ¹æ³åæ°
@return æ¹æ³è¿åå¼
@throws Throwable å¦ææ¹æ³æ§è¡æåºå¼å¸¸
/
    @Nullable
    Object proceed(@NotNull Object[] args) throws Throwable;
}

