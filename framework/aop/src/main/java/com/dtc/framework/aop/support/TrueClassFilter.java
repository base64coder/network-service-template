package com.dtc.framework.aop.support;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.ClassFilter;

/**
     * å§ç»å¹éçç±»è¿æ»¤å¨
@author Network Service Template
/
public class TrueClassFilter implements ClassFilter {

    public static final TrueClassFilter INSTANCE = new TrueClassFilter();

    private TrueClassFilter() {
    }

    @Override
    public boolean matches(@NotNull Class<?> clazz) {
        return true;
    }
}

