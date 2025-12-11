package com.dtc.framework.aop;

public interface TargetSource {
    Class<?> getTargetClass();
    Object getTarget() throws Exception;
    default void releaseTarget(Object target) throws Exception {}
}

