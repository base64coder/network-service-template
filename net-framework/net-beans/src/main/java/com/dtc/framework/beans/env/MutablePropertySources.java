package com.dtc.framework.beans.env;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MutablePropertySources implements Iterable<PropertySource<?>> {
    private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>();

    public void addFirst(PropertySource<?> propertySource) {
        propertySourceList.add(0, propertySource);
    }

    public void addLast(PropertySource<?> propertySource) {
        propertySourceList.add(propertySource);
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return propertySourceList.iterator();
    }
}

