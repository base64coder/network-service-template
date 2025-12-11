package com.dtc.framework.beans.module;

public interface Binder {
    <T> AnnotatedBindingBuilder<T> bind(Class<T> type);
    void install(Module module);
}

