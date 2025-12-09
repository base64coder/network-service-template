package com.dtc.framework.aop.intercept;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * æ¹æ³æ¦æªå¨æ¥å£
æ¦æªæ¹æ³è°ç¨ï¼å¯ä»¥å¨è°ç¨ååæ§è¡é¢å¤é»è¾
@author Network Service Template
/
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {

    /**
     * æ¦æªæ¹æ³è°ç¨
@param invocation æ¹æ³è°ç¨å¯¹è±¡
@return æ¹æ³è°ç¨çè¿åå¼
@throws Throwable å¦ææ¦æªå¨æç®æ å¯¹è±¡æåºå¼å¸¸
/
    @Nullable
    Object invoke(@NotNull MethodInvocation invocation) throws Throwable;
}

