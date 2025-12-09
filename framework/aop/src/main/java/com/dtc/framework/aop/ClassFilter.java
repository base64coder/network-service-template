package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

/**
     * ç±»è¿æ»¤å¨æ¥å£
ç¨äºå¤æ­ç±»æ¯å¦å¹éåç¹æ¡ä»¶
@author Network Service Template
/
@FunctionalInterface
public interface ClassFilter {

    /**
     * å¤æ­ç±»æ¯å¦å¹é
@param clazz è¦æ£æ¥çç±»
@return å¦æå¹éè¿å trueï¼å¦åè¿å false
/
    boolean matches(@NotNull Class<?> clazz);

    /**
     * å§ç»å¹éçç±»è¿æ»¤å¨
/
    ClassFilter TRUE = support.TrueClassFilter.INSTANCE;
}

