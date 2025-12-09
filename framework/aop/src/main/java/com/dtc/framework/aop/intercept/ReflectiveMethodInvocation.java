package com.dtc.framework.aop.intercept;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 反射方法调用实现
 * 借鉴 Spring ReflectiveMethodInvocation 的设计
 * 
 * @author Network Service Template
 */
public class ReflectiveMethodInvocation implements MethodInvocation {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectiveMethodInvocation.class);
    
    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final List<MethodInterceptor> interceptors;
    private int currentInterceptorIndex = -1;
    
    public ReflectiveMethodInvocation(@NotNull Object target, @NotNull Method method, 
                                      @Nullable Object[] arguments,
                                      @NotNull List<MethodInterceptor> interceptors) {
        this.target = target;
        this.method = method;
        this.arguments = arguments != null ? arguments : new Object[0];
        this.interceptors = interceptors;
    }
    
    @Override
    @NotNull
    public Method getMethod() {
        return method;
    }
    
    @Override
    @NotNull
    public Object[] getArgs() {
        return arguments.clone();
    }
    
    @Override
    @NotNull
    public Object getThis() {
        return target;
    }
    
    @Override
    @Nullable
    public Object proceed() throws Throwable {
        // 如果所有拦截器都已执行，调用目标方法
        if (currentInterceptorIndex == interceptors.size() - 1) {
            return invokeJoinpoint();
        }
        
        // 执行下一个拦截器
        MethodInterceptor interceptor = interceptors.get(++currentInterceptorIndex);
        return interceptor.invoke(this);
    }
    
    /**
     * 调用连接点（目标方法）
     */
    @Nullable
    protected Object invokeJoinpoint() throws Throwable {
        method.setAccessible(true);
        return method.invoke(target, arguments);
    }
    
    @Override
    @NotNull
    public Object getStaticPart() {
        return method;
    }
}

