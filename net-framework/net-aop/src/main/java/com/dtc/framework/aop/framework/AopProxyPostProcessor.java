package com.dtc.framework.aop.framework;

import com.dtc.framework.aop.Advisor;
import com.dtc.framework.aop.PointcutAdvisor;
import com.dtc.framework.aop.support.SingletonTargetSource;
import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.beans.exception.BeansException;
import com.dtc.framework.beans.factory.BeanFactory;
import com.dtc.framework.beans.factory.BeanPostProcessor;
import com.dtc.framework.beans.factory.config.BeanDefinition;
import com.dtc.framework.beans.factory.support.DefaultListableBeanFactory;
import com.dtc.framework.aop.MethodMatcher;
import com.dtc.framework.aop.Pointcut;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/**
 * AOP代理处理器
 * 支撑特性 7 (AOP拦截体系)
 */
public class AopProxyPostProcessor implements BeanPostProcessor {
    
    @Inject
    private BeanFactory beanFactory;
    
    private final DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Avoid proxying infrastructure beans
        if (bean instanceof Advisor || bean instanceof AdvisedSupport || bean instanceof AopProxyFactory) {
            return bean;
        }
        
        // Find eligible advisors
        List<Advisor> advisors = findEligibleAdvisors(bean.getClass());
        if (advisors.isEmpty()) {
            return bean;
        }
        
        // Create proxy
        AdvisedSupport advised = new AdvisedSupport();
        advised.setTargetSource(new SingletonTargetSource(bean));
        for (Advisor advisor : advisors) {
            advised.addAdvisor(advisor);
        }
        
        try {
            return proxyFactory.createAopProxy(advised).getProxy();
        } catch (Exception e) {
            throw new BeansException("Failed to create AOP proxy for bean " + beanName, e);
        }
    }
    
    private List<Advisor> findEligibleAdvisors(Class<?> beanClass) {
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory lbf = (DefaultListableBeanFactory) beanFactory;
            String[] beanNames = lbf.getBeanDefinitionNames();
            
            for (String name : beanNames) {
                try {
                    BeanDefinition bd = lbf.getBeanDefinition(name);
                    if (bd == null) continue;
                    
                    Class<?> type = bd.getBeanClass();
                    if (type != null && Advisor.class.isAssignableFrom(type)) {
                        Advisor advisor = (Advisor) lbf.getBean(name);
                        if (canApply(advisor, beanClass)) {
                            eligibleAdvisors.add(advisor);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        return eligibleAdvisors;
    }
    
    private boolean canApply(Advisor advisor, Class<?> targetClass) {
        if (advisor instanceof PointcutAdvisor) {
            PointcutAdvisor pa = (PointcutAdvisor) advisor;
            Pointcut pc = pa.getPointcut();
            if (!pc.getClassFilter().matches(targetClass)) {
                return false;
            }
            
            // Check if any method matches
            MethodMatcher mm = pc.getMethodMatcher();
            for (Method method : targetClass.getMethods()) { // Check all public methods (including inherited)
                if (mm.matches(method, targetClass)) {
                    return true;
                }
            }
            for (Method method : targetClass.getDeclaredMethods()) { // Check declared (protected/private)
                if (mm.matches(method, targetClass)) {
                    return true;
                }
            }
            
            return false;
        }
        return true;
    }
    
    // Placeholder for non-interface method to avoid compilation error if needed
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}


