package com.dtc.core.framework.ioc.annotation;

public class OnClassCondition implements Condition {
    private final String[] classNames;
    
    public OnClassCondition(String[] classNames) {
        this.classNames = classNames;
    }
    
    @Override
    public boolean matches() {
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

