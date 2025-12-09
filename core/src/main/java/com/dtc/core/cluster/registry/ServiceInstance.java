package com.dtc.core.cluster.registry;

import java.util.Map;
import java.util.Objects;

/**
 * 服务实例定义
 * 
 * @author Network Service Template
 */
public class ServiceInstance {
    
    private String serviceId;
    private String serviceName;
    private String host;
    private int port;
    private Map<String, String> metadata;
    private long lastHeartbeat;
    private InstanceStatus status = InstanceStatus.UP;
    
    public ServiceInstance() {
    }
    
    public ServiceInstance(String serviceId, String serviceName, String host, int port) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public enum InstanceStatus {
        UP,
        DOWN,
        STARTING,
        OUT_OF_SERVICE
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public InstanceStatus getStatus() {
        return status;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
}

