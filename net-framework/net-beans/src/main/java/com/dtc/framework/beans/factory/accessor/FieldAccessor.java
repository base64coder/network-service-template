package com.dtc.framework.beans.factory.accessor;

/**
 * 字段访问器接口，用于替代 Field.set
 */
public interface FieldAccessor {
    /**
     * 设置字段值
     * @param target 目标对象
     * @param value 属性值
     */
    void set(Object target, Object value);
}

