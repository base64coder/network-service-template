package com.dtc.annotations.persistence;

/**
 * 数据加载策略枚举
 * 
 * @author Network Service Template
 */
public enum FetchType {
    
    /**
     * 立即加载
     * 查询主实体时，立即加载关联实体
     */
    EAGER,
    
    /**
     * 延迟加载
     * 只有在访问关联实体时才加载
     */
    LAZY
}
