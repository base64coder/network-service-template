package com.dtc.ioc.core.binder;

import com.dtc.ioc.core.BeanScope;
import com.dtc.ioc.core.provider.Provider;

/**
 * 绑定器接口
 * 用于在模块中配置绑定关系
 * 
 * @author Network Service Template
 */
public interface Binder {
    
    /**
     * 开始绑定特定类型
     */
    <T> LinkedBindingBuilder<T> bind(Class<T> type);
    
    /**
     * 安装另一个模块
     */
    void install(com.dtc.ioc.core.NetModule module);
}

