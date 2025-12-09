package com.dtc.framework.aop.proxy;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * AOP 代理工厂
 * 负责创建 AOP 代理对象
 * 借鉴 Spring AOP ProxyFactory 的设计
 * 
 * @author Network Service Template
 */
public class AopProxyFactory {
    
    private static final Logger log = LoggerFactory.getLogger(AopProxyFactory.class);
    
    /**
     * 创建 JDK 动态代理
     * 
     * @param target 目标对象
     * @param interceptors 拦截器列表
     * @return 代理对象
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T createJdkProxy(@NotNull T target, @NotNull List<MethodInterceptor> interceptors) {
        Class<?>[] interfaces = target.getClass().getInterfaces();
        
        if (interfaces.length == 0) {
            // 如果没有接口，使用 ByteBuddy 创建子类代理
            return createByteBuddyProxy(target, interceptors);
        }
        
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new JdkDynamicAopProxy(target, interceptors)
        );
    }
    
    /**
     * 创建 ByteBuddy 代理（用于没有接口的类）
     * 
     * @param target 目标对象
     * @param interceptors 拦截器列表
     * @return 代理对象
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T createByteBuddyProxy(@NotNull T target, @NotNull List<MethodInterceptor> interceptors) {
        try {
            Class<?> targetClass = target.getClass();
            
            Class<?> proxyClass = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new ByteBuddyMethodInterceptor(target, interceptors)))
                    .make()
                    .load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            
            return (T) proxyClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            log.error("Failed to create ByteBuddy proxy for: {}", target.getClass().getName(), e);
            return target; // 回退到原始对象
        }
    }
    
    /**
     * 创建代理（自动选择 JDK 或 ByteBuddy）
     * 
     * @param target 目标对象
     * @param interceptors 拦截器列表
     * @return 代理对象
     */
    @NotNull
    public static <T> T createProxy(@NotNull T target, @NotNull List<MethodInterceptor> interceptors) {
        if (interceptors.isEmpty()) {
            return target; // 没有拦截器，直接返回原对象
        }
        
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length > 0) {
            return createJdkProxy(target, interceptors);
        } else {
            return createByteBuddyProxy(target, interceptors);
        }
    }
}

