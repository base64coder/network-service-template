package com.dtc.framework.aop.support;

import com.dtc.framework.aop.TargetSource;

public class SingletonTargetSource implements TargetSource {
    private final Object target;

    public SingletonTargetSource(Object target) {
        this.target = target;
    }

    @Override
    public Class<?> getTargetClass() {
        return target.getClass();
    }

    @Override
    public Object getTarget() {
        return target;
    }
}

