package com.dtc.annotations.persistence;

/**
 * 级联操作类型枚举
 * 
 * @author Network Service Template
 */
public enum CascadeType {
    
    /**
     * 所有级联操作
     */
    ALL,
    
    /**
     * 持久化（保存）时级联
     */
    PERSIST,
    
    /**
     * 合并（更新）时级联
     */
    MERGE,
    
    /**
     * 删除时级联
     */
    REMOVE,
    
    /**
     * 刷新时级联
     */
    REFRESH,
    
    /**
     * 分离时级联
     */
    DETACH
}
