package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

/**
     * åç¹é¡¾é®æ¥å£
å°åç¹åéç¥ç»åå¨ä¸èµ·
@author Network Service Template
 */
public interface PointcutAdvisor extends Advisor {

    /**
     * è·ååç¹
@return åç¹å¯¹è±¡
 */
    @NotNull
    Pointcut getPointcut();
}

