package com.dtc.framework.aop.support;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.Advice;
import com.dtc.framework.aop.Advisor;
import com.dtc.framework.aop.Pointcut;
import com.dtc.framework.aop.PointcutAdvisor;

/**
 * 默认切点顾问实现
 * 
 * @author Network Service Template
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {
    
    private final Pointcut pointcut;
    private final Advice advice;
    private final int order;
    
    public DefaultPointcutAdvisor(@NotNull Pointcut pointcut, @NotNull Advice advice, int order) {
        this.pointcut = pointcut;
        this.advice = advice;
        this.order = order;
    }
    
    @Override
    @NotNull
    public Pointcut getPointcut() {
        return pointcut;
    }
    
    @Override
    @NotNull
    public Advice getAdvice() {
        return advice;
    }
    
    public int getOrder() {
        return order;
    }
}

