package com.dtc.core.bootstrap.ioc;

import com.google.inject.Module;
import java.util.Collection;

/**
 * 模块提供者接口
 * 用于通过 ServiceLoader 动态加载模块
 */
public interface ModuleProvider {
    /**
     * 获取模块集合
     * @return 模块列表
     */
    Collection<Module> getModules();
}

