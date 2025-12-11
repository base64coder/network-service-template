package com.dtc.core.framework.ioc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
    String prefix() default "";
    boolean ignoreUnknownFields() default true;
}

