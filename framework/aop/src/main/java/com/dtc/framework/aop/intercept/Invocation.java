package com.dtc.framework.aop.intercept;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * è°ç¨æ¥å£
è¡¨ç¤ºä¸ä¸ªè¿æ¥ç¹
@author Network Service Template
/
public interface Invocation extends Joinpoint {

    /**
     * è·åæ¹æ³åæ°
@return æ¹æ³åæ°æ°ç»
/
    @NotNull
    Object[] getArguments();
}

