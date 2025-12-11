package com.dtc.framework.beans.annotation;

public class OnClassCondition implements Condition {
    private final String[] classNames;
    
    public OnClassCondition(String[] classNames) {
        this.classNames = classNames;
    }
    
    public OnClassCondition() {
        // 用于注解实例化
        this.classNames = new String[0];
    }
    
    public OnClassCondition(ConditionalOnClass annotation) {
        this.classNames = annotation.value();
    }
    
    @Override
    public boolean matches() {
        if (classNames.length == 0) return true;
        
        for (String className : classNames) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return true;
    }
}

