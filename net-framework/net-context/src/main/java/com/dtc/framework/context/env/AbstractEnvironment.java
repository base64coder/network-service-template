package com.dtc.framework.context.env;

import com.dtc.framework.beans.env.Environment;
import com.dtc.framework.beans.env.MutablePropertySources;
import com.dtc.framework.beans.env.PropertySource;

import java.util.*;

public class AbstractEnvironment implements Environment {
    private final MutablePropertySources propertySources = new MutablePropertySources();
    private final Set<String> activeProfiles = new LinkedHashSet<>();
    private final Set<String> defaultProfiles = new LinkedHashSet<>(Collections.singleton("default"));

    @Override
    public String[] getActiveProfiles() {
        return activeProfiles.toArray(new String[0]);
    }

    @Override
    public String[] getDefaultProfiles() {
        return defaultProfiles.toArray(new String[0]);
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        for (String profile : profiles) {
            if (activeProfiles.contains(profile) || (activeProfiles.isEmpty() && defaultProfiles.contains(profile))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsProperty(String key) {
        return getProperty(key) != null;
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        for (PropertySource<?> source : propertySources) {
            Object value = source.getProperty(key);
            if (value != null) {
                // TODO: ConversionService
                if (targetType.isInstance(value)) {
                    return targetType.cast(value);
                }
                if (targetType == String.class) {
                    return (T) String.valueOf(value);
                }
                // Simple conversions
                if (targetType == Integer.class || targetType == int.class) return (T) Integer.valueOf(value.toString());
                if (targetType == Boolean.class || targetType == boolean.class) return (T) Boolean.valueOf(value.toString());
                if (targetType == Long.class || targetType == long.class) return (T) Long.valueOf(value.toString());
            }
        }
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return value != null ? value : defaultValue;
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (text == null) return null;
        // Simple ${key} resolution
        // Use a simple StringBuilder loop
        StringBuilder buf = new StringBuilder(text);
        int startIndex = text.indexOf("${");
        while (startIndex != -1) {
            int endIndex = buf.indexOf("}", startIndex + 2);
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + 2, endIndex);
                String defaultValue = null;
                int separatorIndex = placeholder.indexOf(":");
                if (separatorIndex != -1) {
                    defaultValue = placeholder.substring(separatorIndex + 1);
                    placeholder = placeholder.substring(0, separatorIndex);
                }
                
                String propVal = getProperty(placeholder);
                if (propVal == null) {
                    propVal = defaultValue;
                }
                
                if (propVal != null) {
                    buf.replace(startIndex, endIndex + 1, propVal);
                    startIndex = buf.indexOf("${", startIndex + propVal.length());
                } else {
                    startIndex = buf.indexOf("${", endIndex + 1);
                }
            } else {
                startIndex = -1;
            }
        }
        return buf.toString();
    }
    
    public MutablePropertySources getPropertySources() {
        return propertySources;
    }
    
    public void addActiveProfile(String profile) {
        this.activeProfiles.add(profile);
    }
}

