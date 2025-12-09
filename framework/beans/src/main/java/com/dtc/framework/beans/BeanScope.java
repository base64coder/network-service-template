package com.dtc.framework.beans;

/**
 * Bean 作用域枚举
 * 借鉴 Spring Bean Scope 的设计
 * 
 * @author Network Service Template
 */
public enum BeanScope {
    
    /**
     * 单例作用域
     * 整个容器中只有一个实例
     */
    SINGLETON,
    
    /**
     * 原型作用域
     * 每次获取都创建新实例
     */
    PROTOTYPE,
    
    /**
     * 请求作用域
     * 每个 HTTP 请求一个实例
     */
    REQUEST,
    
    /**
     * 会话作用域
     * 每个 HTTP 会话一个实例
     */
    SESSION
}
