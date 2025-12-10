package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认可配置Bean工厂实现
 * 借鉴 ConfigurableBeanFactory 的设计
 * 
 * @author Network Service Template
 */
public class DefaultConfigurableBeanFactory implements ConfigurableBeanFactory {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurableBeanFactory.class);
    
    // Bean定义注册表
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    
    // Bean实例缓存
    private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    
    // Bean后处理器列表
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();
    
    // 属性编辑器注册器列表
    private final List<PropertyEditorRegistrar> propertyEditorRegistrars = new CopyOnWriteArrayList<>();
    
    // 类加载器
    private ClassLoader beanClassLoader;
    
    // Bean表达式解析器
    private BeanExpressionResolver beanExpressionResolver;
    
    // 依赖注入器
    private DependencyInjector dependencyInjector;
    
    public DefaultConfigurableBeanFactory() {
        // 延迟初始化依赖注入器，避免循环依赖
        this.dependencyInjector = null;
    }
    
    /**
     * 获取依赖注入器，如果未初始化则创建
     */
    private DependencyInjector getDependencyInjector() {
        if (dependencyInjector == null) {
            // 创建一个临时的NetApplicationContext来避免循环依赖
            NetApplicationContext tempContext = new NetApplicationContext() {
                @Override
                public <T> T getBean(Class<T> beanType) {
                    return null;
                }
                
                @Override
                public Object getBean(String beanName) {
                    return null;
                }
                
                @Override
                public <T> T getBean(String beanName, Class<T> beanType) {
                    return null;
                }
                
                @Override
                public <T> Map<String, T> getBeansOfType(Class<T> beanType) {
                    return new ConcurrentHashMap<>();
                }
                
                @Override
                public boolean containsBean(String beanName) {
                    return false;
                }
                
                @Override
                public boolean isSingleton(String beanName) {
                    return false;
                }
                
                @Override
                public Class<?> getType(String beanName) {
                    return null;
                }
                
                @Override
                public String[] getBeanDefinitionNames() {
                    return new String[0];
                }
                
                @Override
                public void registerBean(String beanName, Class<?> beanClass) {
                    // 空实现
                }
                
                @Override
                public void registerBean(String beanName, Object beanInstance) {
                    // 空实现
                }
                
                @Override
                public void refresh() {
                    // 空实现
                }
                
                @Override
                public void close() {
                    // 空实现
                }
                
                @Override
                public boolean isActive() {
                    return false;
                }
                
                @Override
                public void publishEvent(ApplicationEvent event) {
                    // 空实现
                }
                
                @Override
                public void addApplicationListener(ApplicationListener<?> listener) {
                    // 空实现
                }
                
                @Override
                public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
                    // 空实现
                }
                
                @Override
                public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
                    // 空实现
                }
            };
            this.dependencyInjector = new DefaultDependencyInjector(tempContext);
        }
        return dependencyInjector;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanType) {
        if (beanType == null) {
            throw new IllegalArgumentException("Bean type cannot be null");
        }
        
        // 查找匹配的Bean定义
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (beanType.isAssignableFrom(definition.getBeanClass())) {
                return (T) getBean(definition.getBeanName());
            }
        }
        
        return null;
    }
    
    @Override
    @Nullable
    public Object getBean(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Bean name cannot be null or empty");
        }
        
        // 检查单例缓存
        if (singletonBeans.containsKey(name)) {
            return singletonBeans.get(name);
        }
        
        // 获取Bean定义
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition == null) {
            return null;
        }
        
        // 创建Bean实例
        Object bean = createBean(name, definition);
        
        // 如果是单例，缓存实例
        if (definition.isSingleton() && bean != null) {
            singletonBeans.put(name, bean);
        }
        
        return bean;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) {
        Object bean = getBean(name);
        if (bean != null && requiredType.isAssignableFrom(bean.getClass())) {
            return (T) bean;
        }
        return null;
    }
    
    @Override
    public boolean containsBean(String name) {
        return beanDefinitions.containsKey(name) || singletonBeans.containsKey(name);
    }
    
    @Override
    public boolean isSingleton(String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        return definition != null && definition.isSingleton();
    }
    
    @Override
    @Nullable
    public Class<?> getType(String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        return definition != null ? definition.getBeanClass() : null;
    }
    
    @Override
    @NotNull
    public String[] getAliases(String name) {
        // 简化实现，暂不支持别名
        return new String[0];
    }
    
    @Override
    public void preInstantiateSingletons() {
        log.info("⚙️ Pre-instantiating singleton beans...");
        
        for (String beanName : beanDefinitions.keySet()) {
            BeanDefinition definition = beanDefinitions.get(beanName);
            if (definition.isSingleton() && !definition.isLazyInit()) {
                getBean(beanName);
            }
        }
        
        log.info("✅ Singleton beans pre-instantiated successfully");
    }
    
    @Override
    public void destroySingletons() {
        log.info("🔄 Destroying singleton beans...");
        
        for (String beanName : beanDefinitions.keySet()) {
            BeanDefinition definition = beanDefinitions.get(beanName);
            if (definition.isSingleton()) {
                try {
                    Object bean = singletonBeans.get(beanName);
                    if (bean != null) {
                        destroyBean(bean, definition);
                    }
                } catch (Exception e) {
                    log.error("❌ Error destroying singleton bean: {}", beanName, e);
                }
            }
        }
        
        singletonBeans.clear();
        log.info("✅ Singleton beans destroyed successfully");
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
        log.debug("⚙️ Bean class loader set: {}", beanClassLoader);
    }
    
    @Override
    public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
        this.beanExpressionResolver = resolver;
        log.debug("⚙️ Bean expression resolver set: {}", resolver);
    }
    
    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
        if (registrar != null) {
            propertyEditorRegistrars.add(registrar);
            log.debug("⚙️ Property editor registrar added: {}", registrar);
        }
    }
    
    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        if (beanPostProcessor != null) {
            beanPostProcessors.add(beanPostProcessor);
            log.debug("⚙️ Bean post processor added: {}", beanPostProcessor.getClass().getSimpleName());
        }
    }
    
    @Override
    public int getBeanPostProcessorCount() {
        return beanPostProcessors.size();
    }
    
    /**
     * 创建Bean实例
     */
    @Nullable
    private Object createBean(String beanName, BeanDefinition definition) {
        try {
            log.debug("⚙️ Creating bean: {}", beanName);
            
            // 实例化Bean
            Object bean = instantiateBean(definition);
            if (bean == null) {
                return null;
            }
            
            // 执行Bean后处理器（初始化前）
            bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
            
            // 注入依赖
            getDependencyInjector().injectDependencies(bean, definition);
            
            // 调用初始化方法
            initializeBean(bean, definition);
            
            // 执行Bean后处理器（初始化后）
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
            
            log.debug("✅ Bean created successfully: {}", beanName);
            return bean;
            
        } catch (Exception e) {
            log.error("❌ Error creating bean: {}", beanName, e);
            return null;
        }
    }
    
    /**
     * 实例化Bean
     */
    @Nullable
    private Object instantiateBean(BeanDefinition definition) {
        try {
            Class<?> beanClass = definition.getBeanClass();
            
            // 使用构造函数创建实例
            java.lang.reflect.Constructor<?> constructor = definition.getConstructor();
            if (constructor != null) {
                return constructor.newInstance();
            }
            
            // 使用工厂方法创建实例
            java.lang.reflect.Method factoryMethod = definition.getFactoryMethod();
            if (factoryMethod != null) {
                return factoryMethod.invoke(null);
            }
            
            // 使用默认构造函数
            return beanClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            log.error("❌ Error instantiating bean: {}", definition.getBeanName(), e);
            return null;
        }
    }
    
    /**
     * 执行Bean后处理器（初始化前）
     */
    @Nullable
    private Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            try {
                result = processor.postProcessBeforeInitialization(result, beanName);
                if (result == null) {
                    return null;
                }
            } catch (Exception e) {
                log.error("❌ Error in bean post processor before initialization: {}", processor.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
    
    /**
     * 执行Bean后处理器（初始化后）
     */
    @Nullable
    private Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            try {
                result = processor.postProcessAfterInitialization(result, beanName);
                if (result == null) {
                    return null;
                }
            } catch (Exception e) {
                log.error("❌ Error in bean post processor after initialization: {}", processor.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
    
    /**
     * 初始化Bean
     */
    private void initializeBean(Object bean, BeanDefinition definition) {
        String initMethodName = definition.getInitMethodName();
        if (initMethodName != null && !initMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method initMethod = bean.getClass().getMethod(initMethodName);
                initMethod.invoke(bean);
                log.debug("⚙️ Initialized bean: {} with method: {}", definition.getBeanName(), initMethodName);
            } catch (Exception e) {
                log.error("❌ Error initializing bean: {} with method: {}", definition.getBeanName(), initMethodName, e);
            }
        }
    }
    
    /**
     * 销毁Bean
     */
    private void destroyBean(Object bean, BeanDefinition definition) {
        String destroyMethodName = definition.getDestroyMethodName();
        if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(bean);
                log.debug("⚙️ Destroyed bean: {} with method: {}", definition.getBeanName(), destroyMethodName);
            } catch (Exception e) {
                log.error("❌ Error destroying bean: {} with method: {}", definition.getBeanName(), destroyMethodName, e);
            }
        }
    }
    
    
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
        log.debug("📦 Registered bean definition: {} -> {}", beanName, beanDefinition.getBeanClass().getName());
    }
    
    @Override
    @Nullable
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitions.get(beanName);
    }
    
    @Override
    @NotNull
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return new ConcurrentHashMap<>(beanDefinitions);
    }
    
    @Override
    @NotNull
    public String[] getBeanDefinitionNames() {
        return beanDefinitions.keySet().toArray(new String[0]);
    }
    
    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        singletonBeans.put(beanName, singletonObject);
        log.debug("📦 Registered singleton bean: {} -> {}", beanName, singletonObject.getClass().getName());
    }
    
    @Override
    @Nullable
    public Object getSingleton(String beanName) {
        return singletonBeans.get(beanName);
    }
    
    @Override
    public void addSingleton(String beanName, Object singletonObject) {
        singletonBeans.put(beanName, singletonObject);
    }
    
    @Override
    @NotNull
    public Object getSingletonMutex() {
        return new Object(); // 简化实现
    }
    
    @Override
    @NotNull
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return new CopyOnWriteArrayList<>(beanPostProcessors);
    }
    
    @Override
    @Nullable
    public ClassLoader getBeanClassLoader() {
        return beanClassLoader;
    }
    
    @Override
    @Nullable
    public BeanExpressionResolver getBeanExpressionResolver() {
        return beanExpressionResolver;
    }
    
    @Override
    @Nullable
    public PropertyEditorRegistry getPropertyEditorRegistry() {
        return null; // 简化实现
    }
    
    @Override
    public void setPropertyEditorRegistry(PropertyEditorRegistry propertyEditorRegistry) {
        // 简化实现
    }
    
    @Override
    public void destroyBean(String beanName, Object beanInstance, BeanDefinition definition) {
        String destroyMethodName = definition.getDestroyMethodName();
        if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method destroyMethod = beanInstance.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(beanInstance);
                log.debug("⚙️ Destroyed bean: {} with method: {}", beanName, destroyMethodName);
            } catch (Exception e) {
                log.error("❌ Error destroying bean: {} with method: {}", beanName, destroyMethodName, e);
            }
        }
    }
    
    @Override
    public void clearBeanDefinitions() {
        beanDefinitions.clear();
        singletonBeans.clear();
        beanPostProcessors.clear();
        propertyEditorRegistrars.clear();
        log.info("🧹 Cleared all bean definitions and singletons in BeanFactory");
    }
}
