package com.dtc.annotations.condition;

import com.dtc.ioc.core.BeanDefinitionReader; // Simplified Context access
import java.util.Map;

public interface Condition {
    boolean matches(ConditionContext context, Map<String, Object> metadata);
}

