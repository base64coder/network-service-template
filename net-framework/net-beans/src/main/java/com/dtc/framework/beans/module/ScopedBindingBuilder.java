package com.dtc.framework.beans.module;

import com.dtc.framework.beans.annotation.Scope;

import java.lang.annotation.Annotation;

public interface ScopedBindingBuilder {
    void asEagerSingleton();
    void in(Class<? extends Annotation> scopeAnnotation);
    void in(Scope scope); // 兼容我们的 Scope 注解
}

