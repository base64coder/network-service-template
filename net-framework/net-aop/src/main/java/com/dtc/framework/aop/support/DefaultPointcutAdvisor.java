package com.dtc.framework.aop.support;

import com.dtc.framework.aop.Advice;
import com.dtc.framework.aop.Pointcut;
import com.dtc.framework.aop.PointcutAdvisor;

public class DefaultPointcutAdvisor implements PointcutAdvisor {
    private final Pointcut pointcut;
    private final Advice advice;

    public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }
}

