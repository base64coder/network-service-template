package com.dtc.framework.context.env;

import com.dtc.framework.beans.env.MutablePropertySources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings({"unchecked", "rawtypes"})
public class StandardEnvironment extends AbstractEnvironment {
    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";
    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

    public StandardEnvironment() {
        customizePropertySources(getPropertySources());
    }

    protected void customizePropertySources(MutablePropertySources propertySources) {
        propertySources.addLast(new MapPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, (Map) System.getProperties()));
        propertySources.addLast(new MapPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, (Map) System.getenv()));
        
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties");
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                propertySources.addLast(new MapPropertySource("applicationConfig", (Map) props));
            }
        } catch (IOException e) {
            // ignore
        }
    }
}

