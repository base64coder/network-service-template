package com.dtc.framework.context;

import com.dtc.framework.beans.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    void refresh();
    void close();
    String getApplicationName();
}

