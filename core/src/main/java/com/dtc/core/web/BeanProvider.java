package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import java.util.Map;

/**
 * Bean提供者接口
 * 负责通过IoC容器获取Bean
 * 
 * @author Network Service Template
 */
public interface BeanProvider {
    
    /**
     * 获取指定类型的所有Bean
     * 
     * @param beanType Bean类型
     * @return Bean映射
     */
    @NotNull
    <T> Map<String, T> getBeansOfType(@NotNull Class<T> beanType);
    
    /**
     * 获取所有Bean
     * 
     * @return Bean映射
     */
    @NotNull
    Map<String, Object> getAllBeans();
}
