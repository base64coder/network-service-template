package com.dtc.framework.ioc.factory;

import com.dtc.framework.ioc.exception.BeansException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

/**
 * AOP代理处理器
 * 支撑特性 7 (AOP拦截体系)
 */
public class AopProxyPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 简单实现：如果Bean有需要代理的方法，创建代理
        // 实际应用中，这里应该检查是否有@Transactional等注解
        Class<?> beanClass = bean.getClass();
        
        // 检查是否有需要代理的方法（这里简化处理，实际应该检查注解）
        boolean needsProxy = false;
        for (Method method : beanClass.getDeclaredMethods()) {
            // 这里可以检查@Transactional等注解
            if (method.getName().startsWith("save") || method.getName().startsWith("delete")) {
                needsProxy = true;
                break;
            }
        }
        
        if (!needsProxy) {
            return bean;
        }
        
        try {
            // 使用ByteBuddy创建代理
            return new ByteBuddy()
                    .subclass(beanClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new AopInterceptor(bean)))
                    .make()
                    .load(beanClass.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            // 如果代理创建失败，返回原Bean
            return bean;
        }
    }
    
    public static class AopInterceptor {
        private final Object target;
        
        public AopInterceptor(Object target) {
            this.target = target;
        }
        
        @net.bytebuddy.implementation.bind.annotation.RuntimeType
        public Object intercept(@net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args,
                                @net.bytebuddy.implementation.bind.annotation.Origin Method method,
                                @net.bytebuddy.implementation.bind.annotation.SuperCall java.util.concurrent.Callable<?> callable) throws Exception {
            // 前置处理
            System.out.println("AOP: Before " + method.getName());
            
            try {
                // 调用原方法
                Object result = method.invoke(target, args);
                
                // 后置处理
                System.out.println("AOP: After " + method.getName());
                
                return result;
            } catch (Exception e) {
                // 异常处理
                System.out.println("AOP: Exception in " + method.getName() + ": " + e.getMessage());
                throw e;
            }
        }
    }
}

