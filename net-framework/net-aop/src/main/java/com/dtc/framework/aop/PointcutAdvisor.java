package com.dtc.framework.aop;

public interface PointcutAdvisor extends Advisor {
    Pointcut getPointcut();
}

