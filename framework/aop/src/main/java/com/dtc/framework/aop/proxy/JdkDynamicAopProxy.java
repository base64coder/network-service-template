package com.dtc.framework.aop.proxy;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import com.dtc.framework.aop.intercept.ReflectiveMethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * JDK 动态代理实现
 * 
 * @author Network Service Template
 */
public class JdkDynamicAopProxy implements InvocationHandler {
    
    private static final Logger log = LoggerFactory.getLogger(JdkDynamicAopProxy.class);
    
    private final Object target;
    private final List<MethodInterceptor> interceptors;
    
    public JdkDynamicAopProxy(@NotNull Object target, @NotNull List<MethodInterceptor> interceptors) {
        this.target = target;
        this.interceptors = interceptors;
    }
    
    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        // 创建方法调用对象
        MethodInvocation invocation = new ReflectiveMethodInvocation(
                target, method, args, interceptors
        );
        
        // 执行拦截器链
        return invocation.proceed();
    }
}

