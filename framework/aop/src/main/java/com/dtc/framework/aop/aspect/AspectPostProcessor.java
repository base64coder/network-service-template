package com.dtc.framework.aop.aspect;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.annotations.aop.Aspect;
import com.dtc.framework.aop.Advisor;
import com.dtc.framework.aop.PointcutAdvisor;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.proxy.AopProxyFactory;
import com.dtc.framework.aop.support.DefaultPointcutAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * AOP 切面后处理器
 * 扫描切面类，创建代理对象
 * 借鉴 Spring BeanPostProcessor 的设计
 * 
 * @author Network Service Template
 */
public class AspectPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(AspectPostProcessor.class);
    
    private final AspectScanner aspectScanner;
    private final List<Advisor> advisors = new ArrayList<>();
    
    public AspectPostProcessor() {
        this.aspectScanner = new AspectScanner();
    }
    
    /**
     * 扫描并注册切面
     * 
     * @param aspectClass 切面类
     * @param aspectInstance 切面实例
     */
    public void registerAspect(@NotNull Class<?> aspectClass, @NotNull Object aspectInstance) {
        if (!aspectClass.isAnnotationPresent(Aspect.class)) {
            return;
        }
        
        List<Advisor> aspectAdvisors = aspectScanner.scanAspect(aspectClass, aspectInstance);
        advisors.addAll(aspectAdvisors);
        
        // 按优先级排序
        advisors.sort(Comparator.comparingInt(this::getAdvisorOrder));
        
        log.info("Registered aspect {} with {} advisors", aspectClass.getName(), aspectAdvisors.size());
    }
    
    /**
     * 为 Bean 创建代理（如果需要）
     * 
     * @param beanName Bean 名称
     * @param bean Bean 实例
     * @return 代理对象或原对象
     */
    @NotNull
    public Object postProcessAfterInitialization(@NotNull String beanName, @NotNull Object bean) {
        // 查找匹配的 Advisor
        List<MethodInterceptor> interceptors = findMatchingAdvisors(bean);
        
        if (interceptors.isEmpty()) {
            return bean; // 没有匹配的切面，返回原对象
        }
        
        // 创建代理
        Object proxy = AopProxyFactory.createProxy(bean, interceptors);
        log.debug("Created AOP proxy for bean: {}", beanName);
        return proxy;
    }
    
    /**
     * 查找匹配的 Advisor
     */
    @NotNull
    private List<MethodInterceptor> findMatchingAdvisors(@NotNull Object bean) {
        List<MethodInterceptor> interceptors = new ArrayList<>();
        
        for (Advisor advisor : advisors) {
            if (advisor instanceof PointcutAdvisor) {
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                
                // 检查类是否匹配
                if (pointcutAdvisor.getPointcut().getClassFilter().matches(bean.getClass())) {
                    // 简化实现：所有方法都匹配
                    if (pointcutAdvisor.getAdvice() instanceof MethodInterceptor) {
                        interceptors.add((MethodInterceptor) pointcutAdvisor.getAdvice());
                    }
                }
            }
        }
        
        return interceptors;
    }
    
    /**
     * 获取 Advisor 的优先级
     */
    private int getAdvisorOrder(@NotNull Advisor advisor) {
        if (advisor instanceof DefaultPointcutAdvisor) {
            return ((DefaultPointcutAdvisor) advisor).getOrder();
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * 获取所有 Advisor
     */
    @NotNull
    public List<Advisor> getAdvisors() {
        return new ArrayList<>(advisors);
    }
}

