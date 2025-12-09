package com.dtc.framework.aop.support;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.MethodMatcher;

import java.lang.reflect.Method;

/**
     * 濮嬬粓鍖归厤鐨勬柟娉曞尮閰嶅櫒
@author Network Service Template
/
public class TrueMethodMatcher implements MethodMatcher {

    public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

    private TrueMethodMatcher() {
    }

    @Override
    public boolean matches(@NotNull Method method, @NotNull Class<?> targetClass) {
        return true;
    }
}

