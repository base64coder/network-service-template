package com.dtc.framework.aop.framework;

import java.lang.reflect.Proxy;

public class DefaultAopProxyFactory implements AopProxyFactory {
    @Override
    public AopProxy createAopProxy(AdvisedSupport config) throws Exception {
        if (config.isProxyTargetClass()) {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
                return new JdkDynamicAopProxy(config);
            }
            return new ByteBuddyAopProxy(config);
        } else {
            if (config.getTargetClass().isInterface() || Proxy.isProxyClass(config.getTargetClass())) {
                 return new JdkDynamicAopProxy(config);
            }
            // If class and not interface, fallback to ByteBuddy
            return new ByteBuddyAopProxy(config);
        }
    }
}

