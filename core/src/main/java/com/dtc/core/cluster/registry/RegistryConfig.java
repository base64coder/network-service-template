package com.dtc.core.cluster.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心配置
 * 
 * @author Network Service Template
 */
public class RegistryConfig {
    
    private String type; // raft, nacos, zookeeper, local
    private String address; // 连接地址 (e.g., "127.0.0.1:8848" or "192.168.1.10:8080,192.168.1.11:8080")
    private String namespace;
    private String group;
    private Map<String, String> properties = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }
}

