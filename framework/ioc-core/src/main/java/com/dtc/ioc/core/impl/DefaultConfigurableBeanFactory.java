package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
     * é»è®¤å¯éç½®Beanå·¥åå®ç°
åé´Spring ConfigurableBeanFactoryçè®¾è®¡
@author Network Service Template
/
public class DefaultConfigurableBeanFactory implements ConfigurableBeanFactory {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurableBeanFactory.class);
    
    // Beanå®ä¹æ³¨åè¡¨
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    
    // Beanå®ä¾ç¼å­
    private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    
    // Beanåå¤çå¨åè¡¨
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();
    
    // å±æ§ç¼è¾å¨æ³¨åå¨åè¡¨
    private final List<PropertyEditorRegistrar> propertyEditorRegistrars = new CopyOnWriteArrayList<>();
    
    // ç±»å è½½å¨
    private ClassLoader beanClassLoader;
    
    // Beanè¡¨è¾¾å¼è§£æå¨
    private BeanExpressionResolver beanExpressionResolver;
    
    // ä¾èµæ³¨å¥å¨
    private DependencyInjector dependencyInjector;
    
    public DefaultConfigurableBeanFactory() {
        // å»¶è¿åå§åä¾èµæ³¨å¥å¨ï¼é¿åå¾ªç¯ä¾èµ
        this.dependencyInjector = null;
    }
    
    /**
     * è·åä¾èµæ³¨å¥å¨ï¼å¦ææªåå§åååå»º
/
    private DependencyInjector getDependencyInjector() {
        if (dependencyInjector == null) {
            // åå»ºä¸ä¸ªä¸´æ¶çNetworkApplicationContextæ¥é¿åå¾ªç¯ä¾èµ
            NetworkApplicationContext tempContext = new NetworkApplicationContext() {
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
                    // ç©ºå®ç°
                }
                
                @Override
                public void registerBean(String beanName, Object beanInstance) {
                    // ç©ºå®ç°
                }
                
                @Override
                public void refresh() {
                    // ç©ºå®ç°
                }
                
                @Override
                public void close() {
                    // ç©ºå®ç°
                }
                
                @Override
                public boolean isActive() {
                    return false;
                }
                
                @Override
                public void publishEvent(ApplicationEvent event) {
                    // ç©ºå®ç°
                }
                
                @Override
                public void addApplicationListener(ApplicationListener<?> listener) {
                    // ç©ºå®ç°
                }
                
                @Override
                public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
                    // ç©ºå®ç°
                }
                
                @Override
                public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
                    // ç©ºå®ç°
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
        
        // æ¥æ¾å¹éçBeanå®ä¹
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
        
        // æ£æ¥åä¾ç¼å­
        if (singletonBeans.containsKey(name)) {
            return singletonBeans.get(name);
        }
        
        // è·åBeanå®ä¹
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition == null) {
            return null;
        }
        
        // åå»ºBeanå®ä¾
        Object bean = createBean(name, definition);
        
        // å¦ææ¯åä¾ï¼ç¼å­å®ä¾
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
        // ç®åå®ç°ï¼æä¸æ¯æå«å
        return new String[0];
    }
    
    @Override
    public void preInstantiateSingletons() {
        log.info("ðï¸ Pre-instantiating singleton beans...");
        
        for (String beanName : beanDefinitions.keySet()) {
            BeanDefinition definition = beanDefinitions.get(beanName);
            if (definition.isSingleton() && !definition.isLazyInit()) {
                getBean(beanName);
            }
        }
        
        log.info("â Singleton beans pre-instantiated successfully");
    }
    
    @Override
    public void destroySingletons() {
        log.info("ð Destroying singleton beans...");
        
        for (String beanName : beanDefinitions.keySet()) {
            BeanDefinition definition = beanDefinitions.get(beanName);
            if (definition.isSingleton()) {
                try {
                    Object bean = singletonBeans.get(beanName);
                    if (bean != null) {
                        destroyBean(bean, definition);
                    }
                } catch (Exception e) {
                    log.error("â Error destroying singleton bean: {}", beanName, e);
                }
            }
        }
        
        singletonBeans.clear();
        log.info("â Singleton beans destroyed successfully");
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
        log.debug("ð§ Bean class loader set: {}", beanClassLoader);
    }
    
    @Override
    public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
        this.beanExpressionResolver = resolver;
        log.debug("ð§ Bean expression resolver set: {}", resolver);
    }
    
    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
        if (registrar != null) {
            propertyEditorRegistrars.add(registrar);
            log.debug("ð§ Property editor registrar added: {}", registrar);
        }
    }
    
    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        if (beanPostProcessor != null) {
            beanPostProcessors.add(beanPostProcessor);
            log.debug("ð§ Bean post processor added: {}", beanPostProcessor.getClass().getSimpleName());
        }
    }
    
    @Override
    public int getBeanPostProcessorCount() {
        return beanPostProcessors.size();
    }
    
    /**
     * åå»ºBeanå®ä¾
/
    @Nullable
    private Object createBean(String beanName, BeanDefinition definition) {
        try {
            log.debug("ðï¸ Creating bean: {}", beanName);
            
            // å®ä¾åBean
            Object bean = instantiateBean(definition);
            if (bean == null) {
                return null;
            }
            
            // æ§è¡Beanåå¤çå¨ï¼åå§ååï¼
            bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
            
            // æ³¨å¥ä¾èµ
            getDependencyInjector().injectDependencies(bean, definition);
            
            // è°ç¨åå§åæ¹æ³
            initializeBean(bean, definition);
            
            // æ§è¡Beanåå¤çå¨ï¼åå§ååï¼
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
            
            log.debug("â Bean created successfully: {}", beanName);
            return bean;
            
        } catch (Exception e) {
            log.error("â Error creating bean: {}", beanName, e);
            return null;
        }
    }
    
    /**
     * å®ä¾åBean
/
    @Nullable
    private Object instantiateBean(BeanDefinition definition) {
        try {
            Class<?> beanClass = definition.getBeanClass();
            
            // ä½¿ç¨æé å½æ°åå»ºå®ä¾
            java.lang.reflect.Constructor<?> constructor = definition.getConstructor();
            if (constructor != null) {
                return constructor.newInstance();
            }
            
            // ä½¿ç¨å·¥åæ¹æ³åå»ºå®ä¾
            java.lang.reflect.Method factoryMethod = definition.getFactoryMethod();
            if (factoryMethod != null) {
                return factoryMethod.invoke(null);
            }
            
            // ä½¿ç¨é»è®¤æé å½æ°
            return beanClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            log.error("â Error instantiating bean: {}", definition.getBeanName(), e);
            return null;
        }
    }
    
    /**
     * æ§è¡Beanåå¤çå¨ï¼åå§ååï¼
/
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
                log.error("â Error in bean post processor before initialization: {}", processor.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
    
    /**
     * æ§è¡Beanåå¤çå¨ï¼åå§ååï¼
/
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
                log.error("â Error in bean post processor after initialization: {}", processor.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
    
    /**
     * åå§åBean
/
    private void initializeBean(Object bean, BeanDefinition definition) {
        String initMethodName = definition.getInitMethodName();
        if (initMethodName != null && !initMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method initMethod = bean.getClass().getMethod(initMethodName);
                initMethod.invoke(bean);
                log.debug("ð§ Initialized bean: {} with method: {}", definition.getBeanName(), initMethodName);
            } catch (Exception e) {
                log.error("â Error initializing bean: {} with method: {}", definition.getBeanName(), initMethodName, e);
            }
        }
    }
    
    /**
     * éæ¯Bean
/
    private void destroyBean(Object bean, BeanDefinition definition) {
        String destroyMethodName = definition.getDestroyMethodName();
        if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(bean);
                log.debug("ð§ Destroyed bean: {} with method: {}", definition.getBeanName(), destroyMethodName);
            } catch (Exception e) {
                log.error("â Error destroying bean: {} with method: {}", definition.getBeanName(), destroyMethodName, e);
            }
        }
    }
    
    
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
        log.debug("ð Registered bean definition: {} -> {}", beanName, beanDefinition.getBeanClass().getName());
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
        log.debug("ð Registered singleton bean: {} -> {}", beanName, singletonObject.getClass().getName());
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
        return new Object(); // ç®åå®ç°
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
        return null; // ç®åå®ç°
    }
    
    @Override
    public void setPropertyEditorRegistry(PropertyEditorRegistry propertyEditorRegistry) {
        // ç®åå®ç°
    }
    
    @Override
    public void destroyBean(String beanName, Object beanInstance, BeanDefinition definition) {
        String destroyMethodName = definition.getDestroyMethodName();
        if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
            try {
                java.lang.reflect.Method destroyMethod = beanInstance.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(beanInstance);
                log.debug("ð§ Destroyed bean: {} with method: {}", beanName, destroyMethodName);
            } catch (Exception e) {
                log.error("â Error destroying bean: {} with method: {}", beanName, destroyMethodName, e);
            }
        }
    }
    
    @Override
    public void clearBeanDefinitions() {
        beanDefinitions.clear();
        singletonBeans.clear();
        beanPostProcessors.clear();
        propertyEditorRegistrars.clear();
        log.info("ðï¸ Cleared all bean definitions and singletons in BeanFactory");
    }
}
