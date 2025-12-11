package com.dtc.framework.ioc.core;

public class PropertyPropertyValue {
    private final String name;
    private final Object value;
    
    // 用于标记是否是引用类型
    private final boolean isRef; 

    public PropertyPropertyValue(String name, Object value, boolean isRef) {
        this.name = name;
        this.value = value;
        this.isRef = isRef;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public boolean isRef() {
        return isRef;
    }
}

