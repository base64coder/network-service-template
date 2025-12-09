package com.dtc.framework.distributed.rpc;

import com.dtc.framework.distributed.rpc.proto.RpcRequest;
import com.dtc.framework.distributed.rpc.proto.RpcResponse;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 处理器
 * 负责处理实际的 RPC 请求
 */
public class RpcHandler {
    
    private static final Logger log = LoggerFactory.getLogger(RpcHandler.class);
    
    private final RpcProviderRegistry providerRegistry;
    
    public RpcHandler(RpcProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }
    
    public RpcResponse handle(RpcRequest request) {
        RpcResponse.Builder responseBuilder = RpcResponse.newBuilder()
                .setRequestId(request.getRequestId());
        
        try {
            String serviceName = request.getServiceName();
            Object bean = providerRegistry.getServiceBeans().get(serviceName);
            
            if (bean == null) {
                throw new RuntimeException("Service not found: " + serviceName);
            }
            
            String methodName = request.getMethodName();
            Object[] args = deserialize(request.getArguments().toByteArray());
            Class<?>[] parameterTypes = getParameterTypes(args); // Simplification: infer from args
            
            // In a real scenario, we should parse parameterTypes from request.getParameterTypes() (JSON string)
            // to handle null arguments or primitive types correctly.
            // For now, we iterate methods to find match.
            Method method = findMethod(bean.getClass(), methodName, args);
            
            Object result = method.invoke(bean, args);
            
            responseBuilder.setStatus(0);
            if (result != null) {
                responseBuilder.setResult(ByteString.copyFrom(serialize(result)));
            }
            
        } catch (Throwable e) {
            log.error("RPC call failed", e);
            responseBuilder.setStatus(1);
            responseBuilder.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
        
        return responseBuilder.build();
    }
    
    private Method findMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == (args == null ? 0 : args.length)) {
                return method; // Simple match, naive implementation
            }
        }
        throw new NoSuchMethodException(methodName);
    }
    
    // Using Java Serialization for simplicity. In production, use Hessian/Protostuff/Jackson.
    private byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }
    
    private Object[] deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return new Object[0];
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (Object[]) ois.readObject();
        }
    }
    
    private Class<?>[] getParameterTypes(Object[] args) {
        if (args == null) return new Class[0];
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return types;
    }
}

