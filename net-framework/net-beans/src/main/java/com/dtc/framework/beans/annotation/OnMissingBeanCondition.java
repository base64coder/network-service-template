package com.dtc.framework.beans.annotation;

import com.dtc.framework.beans.context.ConditionContext;
import com.dtc.framework.beans.factory.BeanFactory;
import java.lang.reflect.AnnotatedElement;

public class OnMissingBeanCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedElement metadata) {
        if (context == null || context.getBeanFactory() == null) {
            return true;
        }
        BeanFactory beanFactory = context.getBeanFactory();
        
        if (!metadata.isAnnotationPresent(ConditionalOnMissingBean.class)) {
            return true;
        }
        
        ConditionalOnMissingBean annotation = metadata.getAnnotation(ConditionalOnMissingBean.class);
        
        for (Class<?> type : annotation.value()) {
            try {
                if (beanFactory.getBean(type) != null) {
                    return false;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        for (String name : annotation.name()) {
            if (beanFactory.containsBean(name)) {
                return false;
            }
        }
        
        return true;
    }
}

