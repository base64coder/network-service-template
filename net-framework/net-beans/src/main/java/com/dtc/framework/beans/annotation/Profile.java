package com.dtc.framework.beans.annotation;

import com.dtc.framework.beans.context.ProfileCondition;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ProfileCondition.class)
public @interface Profile {
    String[] value();
}

