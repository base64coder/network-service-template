package com.dtc.framework.beans.annotation;

import com.dtc.framework.beans.context.ConditionContext;
import java.lang.reflect.AnnotatedElement;

public class OnClassCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedElement metadata) {
        if (!metadata.isAnnotationPresent(ConditionalOnClass.class)) {
            return true;
        }
        
        ConditionalOnClass annotation = metadata.getAnnotation(ConditionalOnClass.class);
        String[] classNames = annotation.value();
        
        if (classNames.length == 0) return true;
        
        for (String className : classNames) {
            try {
                if (context != null && context.getClassLoader() != null) {
                    Class.forName(className, false, context.getClassLoader());
                } else {
                    Class.forName(className);
                }
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return true;
    }
}

