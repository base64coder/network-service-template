package com.dtc.framework.context;

import com.dtc.framework.beans.factory.ListableBeanFactory;

public interface ApplicationContext extends ListableBeanFactory {
    void refresh();
    void close();
    String getApplicationName();
}

