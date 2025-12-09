package com.dtc.framework.distributed.raft.registry;

import java.io.Serializable;
import java.util.Map;

/**
 * Raft注册中心的操作命令
 */
public class RegistryOperation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        REGISTER,
        DEREGISTER
    }
    
    private Type type;
    private String serviceName;
    private String serviceId;
    private String host;
    private int port;
    private Map<String, String> metadata;
    private long timestamp;

    public static RegistryOperation register(String serviceName, String serviceId, String host, int port, Map<String, String> metadata) {
        RegistryOperation op = new RegistryOperation();
        op.type = Type.REGISTER;
        op.serviceName = serviceName;
        op.serviceId = serviceId;
        op.host = host;
        op.port = port;
        op.metadata = metadata;
        op.timestamp = System.currentTimeMillis();
        return op;
    }

    public static RegistryOperation deregister(String serviceName, String serviceId) {
        RegistryOperation op = new RegistryOperation();
        op.type = Type.DEREGISTER;
        op.serviceName = serviceName;
        op.serviceId = serviceId;
        op.timestamp = System.currentTimeMillis();
        return op;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

