package com.dtc.core.web.filter;

import java.util.Map;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * 过滤器配置接口
 * 提供过滤器的配置信息
 * 
 * @author Network Service Template
 */
public interface FilterConfig {
    
    /**
     * 获取过滤器名称
     * 
     * @return 过滤器名称
     */
    @NotNull
    String getFilterName();
    
    /**
     * 获取初始化参数
     * 
     * @param name 参数名
     * @return 参数值
     */
    @Nullable
    String getInitParameter(@NotNull String name);
    
    /**
     * 获取所有初始化参数
     * 
     * @return 参数映射
     */
    @NotNull
    Map<String, String> getInitParameters();
}

