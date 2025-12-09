package com.dtc.framework.aop.proxy;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.aop.intercept.MethodInterceptor;
import com.dtc.framework.aop.intercept.MethodInvocation;
import com.dtc.framework.aop.intercept.ReflectiveMethodInvocation;
import net.bytebuddy.implementation.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * ByteBuddy 方法拦截器
 * 
 * @author Network Service Template
 */
public class ByteBuddyMethodInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(ByteBuddyMethodInterceptor.class);
    
    private final Object target;
    private final List<MethodInterceptor> interceptors;
    
    public ByteBuddyMethodInterceptor(@NotNull Object target, @NotNull List<MethodInterceptor> interceptors) {
        this.target = target;
        this.interceptors = interceptors;
    }
    
    @RuntimeType
    public Object intercept(@This Object proxy,
                           @Origin Method method,
                           @AllArguments Object[] args,
                           @SuperCall Callable<?> superCall) throws Throwable {
        
        // 创建方法调用对象
        MethodInvocation invocation = new ReflectiveMethodInvocation(
                target, method, args, interceptors
        );
        
        // 执行拦截器链
        return invocation.proceed();
    }
}

