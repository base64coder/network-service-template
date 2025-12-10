package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import java.beans.PropertyEditor;

/**
 * 属性编辑器注册表接口
 * 管理属性编辑器
 * 借鉴Spring PropertyEditorRegistry的设计
 * 
 * @author Network Service Template
 */
public interface PropertyEditorRegistry {
    
    /**
     * 注册属性编辑器
     * @param requiredType 必需类型
     * @param propertyEditor 属性编辑器
     */
    void registerCustomEditor(@NotNull Class<?> requiredType, @NotNull PropertyEditor propertyEditor);
    
    /**
     * 注册属性编辑器（指定属性路径）
     * @param requiredType 必需类型
     * @param propertyPath 属性路径
     * @param propertyEditor 属性编辑器
     */
    void registerCustomEditor(@NotNull Class<?> requiredType, @NotNull String propertyPath, @NotNull PropertyEditor propertyEditor);
    
    /**
     * 查找属性编辑器
     * @param requiredType 必需类型
     * @return 属性编辑器
     */
    PropertyEditor findCustomEditor(@NotNull Class<?> requiredType);
    
    /**
     * 查找属性编辑器（指定属性路径）
     * @param requiredType 必需类型
     * @param propertyPath 属性路径
     * @return 属性编辑器
     */
    PropertyEditor findCustomEditor(@NotNull Class<?> requiredType, @NotNull String propertyPath);
}
