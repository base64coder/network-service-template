package com.dtc.framework.aop;

import com.dtc.api.annotations.NotNull;

/**
     * AOP é¡¾é®æ¥å£
å°éç¥ååç¹ç»åå¨ä¸èµ·
@author Network Service Template
 */
public interface Advisor {

    /**
     * è·åéç¥
@return éç¥å¯¹è±¡
 */
    @NotNull
    Advice getAdvice();

    /**
     * æ¯å¦ä¸ºæ¯ä¸ªå®ä¾çé¡¾é®
@return å¦ææ¯æ¯ä¸ªå®ä¾çé¡¾é®è¿å trueï¼å¦åè¿å false
 */
    default boolean isPerInstance() {
        return true;
    }

    /**
     * ç©ºéç¥å ä½ç¬¦
 */
    Advice EMPTY_ADVICE = new Advice() {};
}

