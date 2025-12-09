package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

import java.lang.reflect.Method;

/**
     * æ¹æ³å¹éå¨æ¥å£
ç¨äºå¤æ­æ¹æ³æ¯å¦å¹éåç¹æ¡ä»¶
@author Network Service Template
/
public interface MethodMatcher {

    /**
     * å¤æ­æ¹æ³æ¯å¦å¹éï¼éæå¹éï¼
@param method è¦æ£æ¥çæ¹æ³
@param targetClass ç®æ ç±»
@return å¦æå¹éè¿å trueï¼å¦åè¿å false
/
    boolean matches(@NotNull Method method, @NotNull Class<?> targetClass);

    /**
     * å¤æ­æ¹æ³æ¯å¦å¹éï¼è¿è¡æ¶å¹éï¼
@param method è¦æ£æ¥çæ¹æ³
@param targetClass ç®æ ç±»
@param args æ¹æ³åæ°
@return å¦æå¹éè¿å trueï¼å¦åè¿å false
/
    default boolean matches(@NotNull Method method, @NotNull Class<?> targetClass, @NotNull Object... args) {
        return matches(method, targetClass);
    }

    /**
     * æ¯å¦ä¸ºè¿è¡æ¶å¹é
@return å¦ææ¯è¿è¡æ¶å¹éè¿å trueï¼å¦åè¿å false
/
    default boolean isRuntime() {
        return false;
    }

    /**
     * å§ç»å¹éçæ¹æ³å¹éå¨
/
    MethodMatcher TRUE = support.TrueMethodMatcher.INSTANCE;
}

