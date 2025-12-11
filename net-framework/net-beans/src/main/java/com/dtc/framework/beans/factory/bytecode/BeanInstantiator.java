package com.dtc.framework.beans.factory.bytecode;

/**
 * Bean 实例化器接口
 * 替代 Constructor.newInstance()
 */
public interface BeanInstantiator {
    Object newInstance();
}

