package com.dtc.framework.beans.factory.accessor;

/**
 * Bean 访问器接口，用于替代 Constructor.newInstance
 */
public interface BeanAccessor {
    /**
     * 创建 Bean 实例
     * @return 新的 Bean 实例
     */
    Object newInstance();
}

