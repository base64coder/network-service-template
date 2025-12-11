package com.dtc.framework.beans.factory.config;

import com.dtc.framework.beans.annotation.ConfigurationProperties;
import com.dtc.framework.beans.exception.BeansException;
import com.dtc.framework.beans.factory.BeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 配置属性绑定处理器
 * 支撑特性 9 (配置绑定)
 */
public class ConfigurationPropertiesBindingPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationPropertiesBindingPostProcessor.class);
    
    private StringValueResolver valueResolver;
    
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (valueResolver == null) {
            return bean;
        }
        
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
                // Use resolver to check value: ${prefix.field}
                try {
                    String resolved = valueResolver.resolveStringValue("${" + propertyKey + "}");
                    // If not resolved, it returns ${...} usually, or we can check null if resolver supports it.
                    // AbstractEnvironment returns ${...} if not found and no default.
                    // We need a way to know if it exists.
                    // AbstractEnvironment returns original string if not resolved?
                    // "resolvePlaceholders" usually leaves unresolved placeholders alone.
                    
                    if (resolved != null && !resolved.equals("${" + propertyKey + "}")) {
                        field.setAccessible(true);
                        Object convertedValue = convertValue(resolved, field.getType());
                        field.set(bean, convertedValue);
                    }
                } catch (Exception e) {
                    log.warn("Failed to bind property {} to field {}", propertyKey, field.getName(), e);
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

