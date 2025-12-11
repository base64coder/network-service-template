package com.dtc.framework.beans.factory;

import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {
    <T> Map<String, T> getBeansOfType(Class<T> type);
    String[] getBeanDefinitionNames();
    int getBeanDefinitionCount();
}

