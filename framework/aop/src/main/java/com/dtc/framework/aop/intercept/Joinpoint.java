package com.dtc.framework.aop.intercept;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * è¿æ¥ç¹æ¥å£
AOP ä¸­çè¿æ¥ç¹ï¼è¡¨ç¤ºç¨åºæ§è¡ä¸­çç¹å®ç¹
@author Network Service Template
/
public interface Joinpoint {

    /**
     * ç»§ç»­æ§è¡è¿æ¥ç¹
@return è¿æ¥ç¹çè¿åå¼
@throws Throwable å¦ææ§è¡æåºå¼å¸¸
/
    @Nullable
    Object proceed() throws Throwable;

    /**
     * è·åç®æ å¯¹è±¡
@return ç®æ å¯¹è±¡
/
    @NotNull
    Object getThis();

    /**
     * è·åéæé¨åï¼éå¸¸æ¯æ¹æ³ï¼
@return éæé¨åå¯¹è±¡
/
    @NotNull
    Object getStaticPart();
}

