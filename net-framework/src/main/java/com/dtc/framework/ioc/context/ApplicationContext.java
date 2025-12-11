package com.dtc.framework.ioc.context;

import com.dtc.framework.ioc.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    void refresh();
    void close();
    String getApplicationName();
}

