package com.dtc.framework.aop.intercept;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.lang.reflect.Method;

/**
     * æ¹æ³è°ç¨æ¥å£
è¡¨ç¤ºä¸ä¸ªæ¹æ³è°ç¨è¿æ¥ç¹
@author Network Service Template
/
public interface MethodInvocation extends Invocation {

    /**
     * è·åè¢«è°ç¨çæ¹æ³
@return æ¹æ³å¯¹è±¡
/
    @NotNull
    Method getMethod();

    /**
     * ç»§ç»­æ§è¡æ¹æ³è°ç¨é¾
@return æ¹æ³è°ç¨çè¿åå¼
@throws Throwable å¦ææ¹æ³è°ç¨æåºå¼å¸¸
/
    @Nullable
    Object proceed() throws Throwable;
}

