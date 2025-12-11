package com.dtc.framework.beans.annotation;

import com.dtc.framework.beans.context.ConditionContext;
import java.lang.reflect.AnnotatedElement;

public interface Condition {
    boolean matches(ConditionContext context, AnnotatedElement metadata);
}
