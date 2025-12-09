package com.dtc.annotations.condition;

import com.dtc.ioc.core.BeanDefinitionReader;
import com.dtc.ioc.core.Environment;

public interface ConditionContext {
    BeanDefinitionReader getRegistry();
    Environment getEnvironment();
}

