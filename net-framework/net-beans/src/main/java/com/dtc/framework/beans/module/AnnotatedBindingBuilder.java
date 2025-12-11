package com.dtc.framework.beans.module;

public interface AnnotatedBindingBuilder<T> extends ScopedBindingBuilder {
    ScopedBindingBuilder to(Class<? extends T> implementation);
    void toInstance(T instance);
}

