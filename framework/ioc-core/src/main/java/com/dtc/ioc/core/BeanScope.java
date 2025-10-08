package com.dtc.ioc.core;

/**
 * Bean作用域枚举
 * 借鉴Spring的作用域设计
 * 
 * @author Network Service Template
 */
public enum BeanScope {
    
    /**
     * 单例作用域 - 容器中只有一个实例
     */
    SINGLETON("singleton"),
    
    /**
     * 原型作用域 - 每次获取都创建新实例
     */
    PROTOTYPE("prototype"),
    
    /**
     * 请求作用域 - 每个HTTP请求一个实例
     */
    REQUEST("request"),
    
    /**
     * 会话作用域 - 每个用户会话一个实例
     */
    SESSION("session");
    
    private final String value;
    
    BeanScope(String value) {
        this.value = value;
    }
    
    /**
     * 获取作用域值
     * 
     * @return 作用域值
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 根据值获取作用域
     * 
     * @param value 作用域值
     * @return 作用域
     */
    public static BeanScope fromValue(String value) {
        for (BeanScope scope : values()) {
            if (scope.value.equals(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown bean scope: " + value);
    }
}
