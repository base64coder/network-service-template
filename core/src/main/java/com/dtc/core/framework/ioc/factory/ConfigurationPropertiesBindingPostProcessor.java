package com.dtc.core.framework.ioc.factory;

import com.dtc.core.framework.ioc.annotation.ConfigurationProperties;
import com.dtc.core.framework.ioc.exception.BeansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * 配置属性绑定处理器
 * 支撑特性 9 (配置绑定)
 */
public class ConfigurationPropertiesBindingPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationPropertiesBindingPostProcessor.class);
    
    private final Properties properties;
    
    public ConfigurationPropertiesBindingPostProcessor(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        
        if (!beanClass.isAnnotationPresent(ConfigurationProperties.class)) {
            return bean;
        }
        
        ConfigurationProperties configProps = beanClass.getAnnotation(ConfigurationProperties.class);
        String prefix = configProps.prefix();
        if (!prefix.isEmpty() && !prefix.endsWith(".")) {
            prefix = prefix + ".";
        }
        
        // 绑定属性
        bindProperties(bean, beanClass, prefix);
        
        return bean;
    }
    
    private void bindProperties(Object bean, Class<?> clazz, String prefix) {
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                String propertyKey = prefix + field.getName();
                String propertyValue = properties.getProperty(propertyKey);
                
                if (propertyValue != null) {
                    try {
                        field.setAccessible(true);
                        Object convertedValue = convertValue(propertyValue, field.getType());
                        field.set(bean, convertedValue);
                    } catch (Exception e) {
                        log.warn("Failed to bind property {} to field {}", propertyKey, field.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
    
    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        return value;
    }
}

