package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import java.beans.PropertyEditor;

/**
 * 属性编辑器注册器接口
 * 注册属性编辑器
 * 借鉴Spring PropertyEditorRegistrar的设计
 * 
 * @author Network Service Template
 */
public interface PropertyEditorRegistrar {
    
    /**
     * 注册属性编辑器
     * 
     * @param registry 属性编辑器注册表
     */
    void registerCustomEditors(@NotNull PropertyEditorRegistry registry);
}
