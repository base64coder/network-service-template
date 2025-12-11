package com.dtc.framework.beans.env;

public abstract class PropertySource<T> {
    protected final String name;
    protected final T source;

    public PropertySource(String name, T source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public T getSource() {
        return source;
    }

    public abstract Object getProperty(String name);
}

