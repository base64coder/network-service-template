package com.dtc.framework.aop.support;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.ClassFilter;
import com.dtc.framework.aop.MethodMatcher;
import com.dtc.framework.aop.Pointcut;

/**
     * å§ç»å¹éçåç¹
@author Network Service Template
/
public class TruePointcut implements Pointcut {

    public static final TruePointcut INSTANCE = new TruePointcut();

    private TruePointcut() {
    }

    @Override
    @NotNull
    public ClassFilter getClassFilter() {
        return TrueClassFilter.INSTANCE;
    }

    @Override
    @NotNull
    public MethodMatcher getMethodMatcher() {
        return TrueMethodMatcher.INSTANCE;
    }
}

