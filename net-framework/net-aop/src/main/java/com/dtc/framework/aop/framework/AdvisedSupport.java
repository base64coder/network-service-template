package com.dtc.framework.aop.framework;

import com.dtc.framework.aop.*;
import com.dtc.framework.aop.framework.adapter.AfterReturningAdviceInterceptor;
import com.dtc.framework.aop.framework.adapter.MethodBeforeAdviceInterceptor;

import java.util.*;

public class AdvisedSupport {
    private TargetSource targetSource;
    private final List<MethodInterceptor> methodInterceptors = new ArrayList<>();
    private final List<Advisor> advisors = new ArrayList<>();
    
    // Config
    private boolean proxyTargetClass = false;

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void addMethodInterceptor(MethodInterceptor interceptor) {
        methodInterceptors.add(interceptor);
    }
    
    public void addAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }
    
    public Class<?> getTargetClass() {
        return targetSource.getTargetClass();
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(java.lang.reflect.Method method, Class<?> targetClass) {
        List<Object> interceptorList = new ArrayList<>();
        
        // Advisors first
        for (Advisor advisor : advisors) {
            if (advisor instanceof PointcutAdvisor) {
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                if (pointcutAdvisor.getPointcut().getClassFilter().matches(targetClass) &&
                    pointcutAdvisor.getPointcut().getMethodMatcher().matches(method, targetClass)) {
                    Advice advice = pointcutAdvisor.getAdvice();
                    if (advice instanceof MethodInterceptor) {
                        interceptorList.add(advice);
                    } else if (advice instanceof MethodBeforeAdvice) {
                        interceptorList.add(new MethodBeforeAdviceInterceptor((MethodBeforeAdvice) advice));
                    } else if (advice instanceof AfterReturningAdvice) {
                        interceptorList.add(new AfterReturningAdviceInterceptor((AfterReturningAdvice) advice));
                    }
                }
            }
        }
        
        // Direct interceptors
        interceptorList.addAll(methodInterceptors);
        
        return interceptorList;
    }
}

