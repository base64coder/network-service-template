package com.dtc.framework.context.env;

import com.dtc.framework.beans.env.PropertySource;

import java.util.Map;

public class MapPropertySource extends PropertySource<Map<String, Object>> {
    public MapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        return source.get(name);
    }
}

