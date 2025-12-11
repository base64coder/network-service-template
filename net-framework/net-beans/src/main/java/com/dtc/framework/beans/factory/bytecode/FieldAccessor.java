package com.dtc.framework.beans.factory.bytecode;

/**
 * 字段访问器接口
 * 替代 Field.set() / Field.get()
 */
public interface FieldAccessor {
    void set(Object bean, Object value);
    Object get(Object bean);
}

