package com.dtc.core.framework.ioc.context;

import com.dtc.core.framework.ioc.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    void refresh();
    void close();
    String getApplicationName();
}

