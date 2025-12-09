package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用上下文接口
 * 整合 Bean 工厂、容器和扫描器
 * 借鉴 Spring ApplicationContext 的设计
 * 
 * @author Network Service Template
 */
public interface ApplicationContext extends BeanFactory, BeanContainer {
    
    /**
     * 刷新上下文
     * 扫描组件、注册 Bean 定义、实例化单例 Bean
     */
    void refresh();
    
    /**
     * 关闭上下文
     * 销毁所有单例 Bean
     */
    void close();
    
    /**
     * 检查上下文是否处于活动状态
     * 
     * @return 是否活动
     */
    boolean isActive();
}

