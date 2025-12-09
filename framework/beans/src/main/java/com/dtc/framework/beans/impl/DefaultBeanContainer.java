package com.dtc.framework.beans.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.beans.BeanContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认 Bean 容器实现
 * 
 * @author Network Service Template
 */
public class DefaultBeanContainer implements BeanContainer {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultBeanContainer.class);
    
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<String>> typeToBeanNames = new ConcurrentHashMap<>();
    
    @Override
    public void registerBean(@NotNull String beanName, @NotNull Object bean) {
        if (beans.containsKey(beanName)) {
            log.warn("Bean {} already exists, replacing with new instance", beanName);
        }
        
        beans.put(beanName, bean);
        
        // 建立类型到Bean名称的映射
        Class<?> beanType = bean.getClass();
        typeToBeanNames.computeIfAbsent(beanType, k -> new ArrayList<>()).add(beanName);
        
        // 也注册到接口类型
        for (Class<?> iface : beanType.getInterfaces()) {
            typeToBeanNames.computeIfAbsent(iface, k -> new ArrayList<>()).add(beanName);
        }
        
        log.debug("Registered bean: {} -> {}", beanName, beanType.getName());
    }
    
    @Override
    @NotNull
    public String registerBean(@NotNull Object bean) {
        String beanName = generateBeanName(bean.getClass());
        registerBean(beanName, bean);
        return beanName;
    }
    
    @Override
    @Nullable
    public Object getBean(@NotNull String beanName) {
        return beans.get(beanName);
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(@NotNull String beanName, @NotNull Class<T> beanType) {
        Object bean = beans.get(beanName);
        if (bean != null && beanType.isInstance(bean)) {
            return (T) bean;
        }
        return null;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(@NotNull Class<T> beanType) {
        List<String> beanNames = typeToBeanNames.get(beanType);
        if (beanNames != null && !beanNames.isEmpty()) {
            // 返回第一个匹配的Bean
            String beanName = beanNames.get(0);
            Object bean = beans.get(beanName);
            if (bean != null && beanType.isInstance(bean)) {
                return (T) bean;
            }
        }
        return null;
    }
    
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeansOfType(@NotNull Class<T> beanType) {
        Map<String, T> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            if (beanType.isInstance(entry.getValue())) {
                result.put(entry.getKey(), (T) entry.getValue());
            }
        }
        
        return result;
    }
    
    @Override
    public boolean containsBean(@NotNull String beanName) {
        return beans.containsKey(beanName);
    }
    
    @Override
    public boolean removeBean(@NotNull String beanName) {
        Object bean = beans.remove(beanName);
        if (bean != null) {
            // 从类型映射中移除
            Class<?> beanType = bean.getClass();
            typeToBeanNames.getOrDefault(beanType, Collections.emptyList()).remove(beanName);
            for (Class<?> iface : beanType.getInterfaces()) {
                typeToBeanNames.getOrDefault(iface, Collections.emptyList()).remove(beanName);
            }
            log.debug("Removed bean: {}", beanName);
            return true;
        }
        return false;
    }
    
    @Override
    @NotNull
    public Set<String> getBeanNames() {
        return new HashSet<>(beans.keySet());
    }
    
    @Override
    @NotNull
    public Map<String, Object> getAllBeans() {
        return new HashMap<>(beans);
    }
    
    @Override
    public void clear() {
        beans.clear();
        typeToBeanNames.clear();
        log.info("All beans cleared");
    }
    
    /**
     * 生成 Bean 名称（首字母小写）
     */
    @NotNull
    private String generateBeanName(@NotNull Class<?> beanClass) {
        String className = beanClass.getSimpleName();
        if (className.isEmpty()) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}

