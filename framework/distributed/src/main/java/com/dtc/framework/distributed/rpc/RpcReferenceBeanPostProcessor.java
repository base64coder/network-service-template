package com.dtc.framework.distributed.rpc;

import com.dtc.api.rpc.RpcReference;
import com.dtc.core.cluster.registry.ServiceDiscovery;
import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.framework.distributed.rpc.proto.RpcRequest;
import com.dtc.framework.distributed.rpc.proto.RpcResponse;
import com.dtc.framework.beans.BeanPostProcessor;
import com.google.protobuf.ByteString;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 处理 @RpcReference 注解，注入动态代理
 */
@Singleton
public class RpcReferenceBeanPostProcessor implements BeanPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(RpcReferenceBeanPostProcessor.class);
    
    // Use Provider to avoid circular dependency or early initialization issues
    private final Provider<ServiceDiscovery> discoveryProvider;
    private final Provider<RpcClient> rpcClientProvider;
    
    @Inject
    public RpcReferenceBeanPostProcessor(Provider<ServiceDiscovery> discoveryProvider, Provider<RpcClient> rpcClientProvider) {
        this.discoveryProvider = discoveryProvider;
        this.rpcClientProvider = rpcClientProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    RpcReference reference = field.getAnnotation(RpcReference.class);
                    Object proxy = createProxy(field.getType(), reference);
                    field.setAccessible(true);
                    try {
                        field.set(bean, proxy);
                    } catch (IllegalAccessException e) {
                        log.error("Failed to inject RPC proxy", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
    
    private Object createProxy(Class<?> interfaceClass, RpcReference reference) {
        try {
            return new ByteBuddy()
                    .subclass(interfaceClass)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(new RpcInvocationHandler(interfaceClass, reference)))
                    .make()
                    .load(interfaceClass.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RPC proxy", e);
        }
    }
    
    private class RpcInvocationHandler implements InvocationHandler {
        private final Class<?> interfaceClass;
        private final RpcReference reference;
        
        public RpcInvocationHandler(Class<?> interfaceClass, RpcReference reference) {
            this.interfaceClass = interfaceClass;
            this.reference = reference;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            
            String serviceName = reference.name().isEmpty() ? interfaceClass.getName() : reference.name();
            
            // 1. Service Discovery
            List<ServiceInstance> instances = discoveryProvider.get().getInstances(serviceName).get(5, TimeUnit.SECONDS);
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No available providers for service: " + serviceName);
            }
            
            // 2. Load Balance (Random)
            ServiceInstance instance = instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
            
            // 3. Construct Request
            RpcRequest request = RpcRequest.newBuilder()
                    .setRequestId(UUID.randomUUID().toString())
                    .setServiceName(serviceName)
                    .setMethodName(method.getName())
                    .setArguments(ByteString.copyFrom(serialize(args)))
                    .setTimeout(reference.timeout())
                    .build();
            
            // 4. Send Request
            RpcResponse response = rpcClientProvider.get()
                    .send(instance.getHost(), instance.getPort(), request)
                    .get(reference.timeout(), TimeUnit.MILLISECONDS);
            
            // 5. Handle Response
            if (response.getStatus() != 0) {
                throw new RuntimeException("RPC error: " + response.getErrorMessage());
            }
            
            return deserialize(response.getResult().toByteArray());
        }
    }
    
    private byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }
    
    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) return null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}

