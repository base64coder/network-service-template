package com.dtc.framework.beans.context;

import com.dtc.framework.beans.env.Environment;
import com.dtc.framework.beans.factory.BeanFactory;

public interface ConditionContext {
    BeanFactory getBeanFactory();
    Environment getEnvironment();
    ClassLoader getClassLoader();
}

