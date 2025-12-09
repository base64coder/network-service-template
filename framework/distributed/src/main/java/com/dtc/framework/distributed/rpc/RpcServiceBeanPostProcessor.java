package com.dtc.framework.distributed.rpc;

import com.dtc.api.rpc.RpcService;
import com.dtc.ioc.core.BeanPostProcessor;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 扫描 @RpcService 注解，并将服务注册到 RpcProviderRegistry
 */
@Singleton
public class RpcServiceBeanPostProcessor implements BeanPostProcessor {
    
    private final RpcProviderRegistry providerRegistry;
    
    @Inject
    public RpcServiceBeanPostProcessor(RpcProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        // 处理代理类情况
        if (beanClass.getName().contains("$$EnhancerByGuice$$") || beanClass.getName().contains("CGLIB")) {
            beanClass = beanClass.getSuperclass();
        }
        
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            String serviceName = rpcService.name();
            if (serviceName == null || serviceName.isEmpty()) {
                // 默认使用第一个接口的名称，如果没有接口则使用类名
                if (beanClass.getInterfaces().length > 0) {
                    serviceName = beanClass.getInterfaces()[0].getName();
                } else {
                    serviceName = beanClass.getName();
                }
            }
            
            providerRegistry.addService(serviceName, bean, rpcService);
        }
        
        return bean;
    }
}

