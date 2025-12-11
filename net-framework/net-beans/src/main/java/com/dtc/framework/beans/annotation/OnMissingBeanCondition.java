package com.dtc.framework.beans.annotation;

import com.dtc.framework.beans.factory.BeanFactory;

public class OnMissingBeanCondition implements Condition {
    private final Class<?>[] types;
    private final String[] names;
    private BeanFactory beanFactory;
    
    public OnMissingBeanCondition() {
        this.types = new Class<?>[0];
        this.names = new String[0];
    }
    
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
            return true;
        }
        
        for (Class<?> type : types) {
            try {
                if (beanFactory.getBean(type) != null) {
                    return false;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        for (String name : names) {
            if (beanFactory.containsBean(name)) {
                return false;
            }
        }
        
        return true;
    }
}

