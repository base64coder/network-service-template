package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.annotations.aop.Aspect;
import com.dtc.annotations.aop.After;
import com.dtc.annotations.aop.AfterReturning;
import com.dtc.annotations.aop.AfterThrowing;
import com.dtc.annotations.aop.Around;
import com.dtc.annotations.aop.Before;
import com.dtc.annotations.aop.Pointcut;
import com.dtc.framework.aop.Advisor;
import com.dtc.framework.aop.PointcutAdvisor;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.support.DefaultPointcutAdvisor;
import com.dtc.framework.aop.support.TruePointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 切面扫描器
 * 扫描 @Aspect 注解的类，解析通知方法，创建 Advisor
 * 借鉴 Spring AspectJ 的设计
 * 
 * @author Network Service Template
 */
public class AspectScanner {
    
    private static final Logger log = LoggerFactory.getLogger(AspectScanner.class);
    
    /**
     * 扫描切面类，创建 Advisor 列表
     * 
     * @param aspectClass 切面类
     * @param aspectInstance 切面实例
     * @return Advisor 列表
     */
    @NotNull
    public List<Advisor> scanAspect(@NotNull Class<?> aspectClass, @NotNull Object aspectInstance) {
        List<Advisor> advisors = new ArrayList<>();
        
        if (!aspectClass.isAnnotationPresent(Aspect.class)) {
            log.warn("Class {} is not annotated with @Aspect", aspectClass.getName());
            return advisors;
        }
        
        Aspect aspectAnnotation = aspectClass.getAnnotation(Aspect.class);
        int aspectOrder = aspectAnnotation.order();
        
        // 扫描所有方法
        Method[] methods = aspectClass.getDeclaredMethods();
        for (Method method : methods) {
            // 扫描 @Before 注解
            if (method.isAnnotationPresent(Before.class)) {
                Before before = method.getAnnotation(Before.class);
                Advisor advisor = createBeforeAdvisor(aspectInstance, method, before.value(), aspectOrder);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
            
            // 扫描 @After 注解
            if (method.isAnnotationPresent(After.class)) {
                After after = method.getAnnotation(After.class);
                Advisor advisor = createAfterAdvisor(aspectInstance, method, after.value(), aspectOrder);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
            
            // 扫描 @AfterReturning 注解
            if (method.isAnnotationPresent(AfterReturning.class)) {
                AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
                Advisor advisor = createAfterReturningAdvisor(aspectInstance, method, 
                        afterReturning.value(), afterReturning.returning(), aspectOrder);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
            
            // 扫描 @AfterThrowing 注解
            if (method.isAnnotationPresent(AfterThrowing.class)) {
                AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
                Advisor advisor = createAfterThrowingAdvisor(aspectInstance, method, 
                        afterThrowing.value(), afterThrowing.throwing(), aspectOrder);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
            
            // 扫描 @Around 注解
            if (method.isAnnotationPresent(Around.class)) {
                Around around = method.getAnnotation(Around.class);
                Advisor advisor = createAroundAdvisor(aspectInstance, method, around.value(), aspectOrder);
                if (advisor != null) {
                    advisors.add(advisor);
                }
            }
        }
        
        log.info("Scanned aspect {} and found {} advisors", aspectClass.getName(), advisors.size());
        return advisors;
    }
    
    /**
     * 创建 @Before 通知的 Advisor
     */
    @NotNull
    private Advisor createBeforeAdvisor(@NotNull Object aspectInstance, @NotNull Method method, 
                                        @NotNull String pointcutExpression, int order) {
        MethodInterceptor interceptor = new BeforeMethodInterceptor(aspectInstance, method);
        return new DefaultPointcutAdvisor(TruePointcut.INSTANCE, interceptor, order);
    }
    
    /**
     * 创建 @After 通知的 Advisor
     */
    @NotNull
    private Advisor createAfterAdvisor(@NotNull Object aspectInstance, @NotNull Method method, 
                                      @NotNull String pointcutExpression, int order) {
        MethodInterceptor interceptor = new AfterMethodInterceptor(aspectInstance, method);
        return new DefaultPointcutAdvisor(TruePointcut.INSTANCE, interceptor, order);
    }
    
    /**
     * 创建 @AfterReturning 通知的 Advisor
     */
    @NotNull
    private Advisor createAfterReturningAdvisor(@NotNull Object aspectInstance, @NotNull Method method, 
                                               @NotNull String pointcutExpression, @NotNull String returning, 
                                               int order) {
        MethodInterceptor interceptor = new AfterReturningMethodInterceptor(aspectInstance, method, returning);
        return new DefaultPointcutAdvisor(TruePointcut.INSTANCE, interceptor, order);
    }
    
    /**
     * 创建 @AfterThrowing 通知的 Advisor
     */
    @NotNull
    private Advisor createAfterThrowingAdvisor(@NotNull Object aspectInstance, @NotNull Method method, 
                                               @NotNull String pointcutExpression, @NotNull String throwing, 
                                               int order) {
        MethodInterceptor interceptor = new AfterThrowingMethodInterceptor(aspectInstance, method, throwing);
        return new DefaultPointcutAdvisor(TruePointcut.INSTANCE, interceptor, order);
    }
    
    /**
     * 创建 @Around 通知的 Advisor
     */
    @NotNull
    private Advisor createAroundAdvisor(@NotNull Object aspectInstance, @NotNull Method method, 
                                       @NotNull String pointcutExpression, int order) {
        MethodInterceptor interceptor = new AroundMethodInterceptor(aspectInstance, method);
        return new DefaultPointcutAdvisor(TruePointcut.INSTANCE, interceptor, order);
    }
}

