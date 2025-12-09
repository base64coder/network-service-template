package com.dtc.framework.beans.context;

import com.dtc.framework.beans.impl.DefaultNetworkApplicationContext;

/**
 * Annotation-based Application Context.
 * Similar to Spring's AnnotationConfigApplicationContext.
 */
public class AnnotationConfigApplicationContext extends DefaultNetworkApplicationContext {

    public AnnotationConfigApplicationContext() {
        super();
    }

    public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
        this();
        register(componentClasses);
        refresh();
    }

    public AnnotationConfigApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }
}

