package com.dtc.core.framework.ioc.annotation;

import com.dtc.core.framework.ioc.factory.BeanFactory;

public class OnMissingBeanCondition implements Condition {
    private final Class<?>[] types;
    private final String[] names;
    private BeanFactory beanFactory;
    
    public OnMissingBeanCondition(Class<?>[] types, String[] names) {
        this.types = types;
        this.names = names;
    }
    
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    @Override
    public boolean matches() {
        if (beanFactory == null) {
            return true; // 默认匹配，等待BeanFactory设置
        }
        
        // 检查类型
        for (Class<?> type : types) {
            try {
                beanFactory.getBean(type);
                return false; // 找到了，不匹配
            } catch (Exception e) {
                // 没找到，继续
            }
        }
        
        // 检查名称
        for (String name : names) {
            if (beanFactory.containsBean(name)) {
                return false; // 找到了，不匹配
            }
        }
        
        return true; // 都没找到，匹配
    }
}

