package com.dtc.framework.beans.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.beans.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 默认应用上下文实现
 * 整合 Bean 工厂、容器和扫描器
 * 
 * @author Network Service Template
 */
public class DefaultApplicationContext implements ApplicationContext {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultApplicationContext.class);
    
    private final BeanContainer beanContainer;
    private final BeanFactory beanFactory;
    private final BeanScanner beanScanner;
    private final BeanDefinitionReader beanDefinitionReader;
    private final com.dtc.framework.beans.condition.ConditionEvaluator conditionEvaluator;
    private boolean active = false;
    private String[] scanBasePackages;
    
    public DefaultApplicationContext(@NotNull String... scanBasePackages) {
        this.scanBasePackages = scanBasePackages;
        this.beanContainer = new DefaultBeanContainer();
        this.beanDefinitionReader = new BeanDefinitionReader();
        DefaultEnvironment environment = new DefaultEnvironment(); // Assuming DefaultEnvironment exists
        this.conditionEvaluator = new com.dtc.framework.beans.condition.ConditionEvaluator(beanDefinitionReader, environment);
        
        DependencyInjector dependencyInjector = new DefaultDependencyInjector(beanContainer);
        this.beanFactory = new DefaultBeanFactory(beanContainer, dependencyInjector);
        this.beanScanner = new BeanScanner();
    }
    
    @Override
    public void refresh() {
        log.info("Refreshing application context...");
        
        try {
            // 扫描组件
            scanComponents();
            
            // 预实例化单例 Bean
            beanFactory.preInstantiateSingletons();
            
            active = true;
            log.info("Application context refreshed successfully");
            
        } catch (Exception e) {
            log.error("Failed to refresh application context", e);
            throw new RuntimeException("Failed to refresh application context", e);
        }
    }
    
    @Override
    public void close() {
        log.info("Closing application context...");
        
        try {
            beanFactory.destroySingletons();
            beanContainer.clear();
            active = false;
            log.info("Application context closed successfully");
            
        } catch (Exception e) {
            log.error("Failed to close application context", e);
        }
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    /**
     * 扫描组件并注册 Bean 定义
     */
    private void scanComponents() {
        if (scanBasePackages == null || scanBasePackages.length == 0) {
            log.warn("No scan base packages specified");
            return;
        }
        
        for (String basePackage : scanBasePackages) {
            log.info("Scanning components in package: {}", basePackage);
            List<Class<?>> components = beanScanner.scanComponents(basePackage);
            
            for (Class<?> componentClass : components) {
                // Check @Conditional
                if (conditionEvaluator.shouldSkip(componentClass)) {
                    log.debug("Skipping component {} due to condition mismatch", componentClass.getName());
                    continue;
                }

                BeanDefinition definition = beanDefinitionReader.readBeanDefinition(componentClass);
                if (definition != null) {
                    ((DefaultBeanFactory) beanFactory).registerBeanDefinition(
                            definition.getBeanName(), definition);
                    log.debug("Registered bean definition: {}", definition.getBeanName());
                }
            }
        }
    }
    
    // ========== BeanFactory 方法 ==========
    
    @Override
    @Nullable
    public Object getBean(@NotNull String name) {
        return beanFactory.getBean(name);
    }
    
    @Override
    @Nullable
    public <T> T getBean(@NotNull String name, @NotNull Class<T> requiredType) {
        return beanFactory.getBean(name, requiredType);
    }
    
    @Override
    @Nullable
    public <T> T getBean(@NotNull Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }
    
    @Override
    public boolean containsBean(@NotNull String name) {
        return beanFactory.containsBean(name);
    }
    
    @Override
    public boolean isSingleton(@NotNull String name) {
        return beanFactory.isSingleton(name);
    }
    
    @Override
    @Nullable
    public Class<?> getType(@NotNull String name) {
        return beanFactory.getType(name);
    }
    
    @Override
    @NotNull
    public String[] getAliases(@NotNull String name) {
        return beanFactory.getAliases(name);
    }
    
    @Override
    public void preInstantiateSingletons() {
        beanFactory.preInstantiateSingletons();
    }
    
    @Override
    public void destroySingletons() {
        beanFactory.destroySingletons();
    }
    
    // ========== BeanContainer 方法 ==========
    
    @Override
    public void registerBean(@NotNull String beanName, @NotNull Object bean) {
        beanContainer.registerBean(beanName, bean);
    }
    
    @Override
    @NotNull
    public String registerBean(@NotNull Object bean) {
        return beanContainer.registerBean(bean);
    }
    
    // BeanContainer 的 getBean 方法已经通过 BeanFactory 实现
    // 这里直接使用 beanContainer 的方法
    
    @Override
    @NotNull
    public <T> Map<String, T> getBeansOfType(@NotNull Class<T> beanType) {
        return beanContainer.getBeansOfType(beanType);
    }
    
    @Override
    public boolean removeBean(@NotNull String beanName) {
        return beanContainer.removeBean(beanName);
    }
    
    @Override
    @NotNull
    public Set<String> getBeanNames() {
        return beanContainer.getBeanNames();
    }
    
    @Override
    @NotNull
    public Map<String, Object> getAllBeans() {
        return beanContainer.getAllBeans();
    }
    
    @Override
    public void clear() {
        beanContainer.clear();
    }
}

