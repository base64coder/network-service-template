package com.dtc.core.framework.ioc.annotation;

import java.lang.annotation.*;

@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inject {
    boolean required() default true;
}

