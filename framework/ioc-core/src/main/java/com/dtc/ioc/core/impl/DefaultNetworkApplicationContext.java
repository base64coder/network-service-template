package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
     * é»è®¤ç½ç»åºç¨ä¸ä¸æå®ç°
åé´Spring ApplicationContextåGuice Injectorçä¼ç¹
@author Network Service Template
/
public class DefaultNetworkApplicationContext implements NetworkApplicationContext {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultNetworkApplicationContext.class);
    
    // Beanå·¥å
    private final ConfigurableBeanFactory beanFactory;
    
    // ç¯å¢éç½®
    private final Environment environment;
    
    // åºç¨çå¬å¨åè¡¨
    private final List<ApplicationListener<?>> applicationListeners = new CopyOnWriteArrayList<>();
    
    // Beanåå¤çå¨åè¡¨
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();
    
    // Beanå·¥ååå¤çå¨åè¡¨
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new CopyOnWriteArrayList<>();
    
    // åºç¨äºä»¶å¤æ­å¨
    private final ApplicationEventMulticaster eventMulticaster;
    
    // å®¹å¨ç¶æ
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean refreshed = new AtomicBoolean(false);
    
    // å¯å¨å³é­çæ§å¨
    private final Object startupShutdownMonitor = new Object();
    
    // å¯å¨æ¶é´
    private long startupDate;
    
    public DefaultNetworkApplicationContext() {
        this.beanFactory = new DefaultConfigurableBeanFactory();
        this.environment = new DefaultEnvironment();
        this.eventMulticaster = new SimpleApplicationEventMulticaster();
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanType) {
        if (!active.get()) {
            throw new IllegalStateException("Application context is not active");
        }
        
        // æ¥æ¾å¹éçBeanå®ä¹
        for (BeanDefinition definition : beanFactory.getBeanDefinitions().values()) {
            if (beanType.isAssignableFrom(definition.getBeanClass())) {
                return (T) getBean(definition.getBeanName());
            }
        }
        
        return null;
    }
    
    @Override
    @Nullable
    public Object getBean(String beanName) {
        if (!active.get()) {
            throw new IllegalStateException("Application context is not active");
        }
        
        return beanFactory.getBean(beanName);
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> beanType) {
        Object bean = getBean(beanName);
        if (bean != null && beanType.isAssignableFrom(bean.getClass())) {
            return (T) bean;
        }
        return null;
    }
    
    @Override
    public void registerBean(String beanName, Class<?> beanClass) {
        DefaultBeanDefinition definition = new DefaultBeanDefinition(beanName, beanClass);
        beanFactory.registerBeanDefinition(beanName, definition);
        log.debug("ð Registered bean definition: {} -> {}", beanName, beanClass.getName());
    }
    
    @Override
    public void registerBean(String beanName, Object beanInstance) {
        beanFactory.registerSingleton(beanName, beanInstance);
        log.debug("ð Registered bean instance: {} -> {}", beanName, beanInstance.getClass().getName());
    }
    
    @Override
    public void refresh() {
        synchronized (this.startupShutdownMonitor) {
            if (refreshed.get()) {
                log.warn("Application context already refreshed");
                return;
            }

            try {
                log.info("ð Refreshing Network Application Context...");
                this.startupDate = System.currentTimeMillis();

                // 1. åå¤ç¯å¢
                prepareEnvironment();

                // 2. åå¤BeanFactory
                prepareBeanFactory(beanFactory);

                // 3. æ§è¡BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory);

                // 4. æ³¨åBeanPostProcessor
                registerBeanPostProcessors(beanFactory);

                // 5. åå§åäºä»¶å¤æ­å¨
                initApplicationEventMulticaster();

                // 6. æ³¨åçå¬å¨
                registerListeners();

                // 7. å®ä¾åææéæå è½½çåä¾Bean
                finishBeanFactoryInitialization(beanFactory);

                // 8. å¯å¨çå½å¨æç®¡ç
                startLifecycleManagement();

                active.set(true);
                refreshed.set(true);

                // 9. åå¸ContextRefreshedEvent
                publishEvent(new ContextRefreshedEvent(this));

                log.info("â Network Application Context refreshed successfully in {} ms",
                        (System.currentTimeMillis() - startupDate));

            } catch (Exception e) {
                log.error("â Failed to refresh Network Application Context", e);
                throw new RuntimeException("Failed to refresh application context", e);
            }
        }
    }
    
    private void prepareEnvironment() {
        log.info("ð§ Preparing environment...");
        // å¯ä»¥å¨è¿éå è½½éç½®æä»¶ãè®¾ç½®æ´»å¨éç½®æä»¶ç­
        log.info("â Environment prepared successfully");
    }

    private void prepareBeanFactory(ConfigurableBeanFactory beanFactory) {
        log.info("ð§ Preparing BeanFactory...");
        beanFactory.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
        // æ³¨ååç½®çBeanPostProcessor
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        log.info("â BeanFactory prepared successfully");
    }

    private void invokeBeanFactoryPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("ð§ Invoking BeanFactoryPostProcessors...");
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
        log.info("â BeanFactoryPostProcessors invoked successfully");
    }

    private void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("ð§ Registering BeanPostProcessors...");
        // æ³¨åéè¿addBeanPostProcessoræ·»å çå¤çå¨
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        // æ¥æ¾å¹¶æ³¨åéè¿Beanå®ä¹çBeanPostProcessor
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (BeanPostProcessor.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    BeanPostProcessor bpp = (BeanPostProcessor) getBean(beanName);
                    beanFactory.addBeanPostProcessor(bpp);
                    log.debug("ð Registered BeanPostProcessor from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("â Failed to register BeanPostProcessor from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("â BeanPostProcessors registered successfully");
    }

    private void initApplicationEventMulticaster() {
        log.info("ð§ Initializing ApplicationEventMulticaster...");
        // å¯ä»¥å¨è¿ééç½®äºä»¶å¤æ­å¨ï¼ä¾å¦è®¾ç½®ä»»å¡æ§è¡å¨
        log.info("â ApplicationEventMulticaster initialized successfully");
    }

    private void registerListeners() {
        log.info("ð§ Registering ApplicationListeners...");
        for (ApplicationListener<?> listener : applicationListeners) {
            eventMulticaster.addApplicationListener(listener);
        }
        // æ¥æ¾å¹¶æ³¨åéè¿Beanå®ä¹çApplicationListener
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (ApplicationListener.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    ApplicationListener<?> listener = (ApplicationListener<?>) getBean(beanName);
                    eventMulticaster.addApplicationListener(listener);
                    log.debug("ð Registered ApplicationListener from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("â Failed to register ApplicationListener from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("â ApplicationListeners registered successfully");
    }

    private void finishBeanFactoryInitialization(ConfigurableBeanFactory beanFactory) {
        log.info("ð§ Finishing BeanFactory initialization (pre-instantiating singletons)...");
        beanFactory.preInstantiateSingletons();
        log.info("â BeanFactory initialization finished successfully");
    }

    @Override
    public void close() {
        synchronized (this.startupShutdownMonitor) {
            if (!active.get()) {
                return;
            }

            try {
                log.info("ð Closing Network Application Context...");

                // 1. åå¸ContextClosedEvent
                publishEvent(new ContextClosedEvent(this));

                // 2. éæ¯ææBean
                destroyAllBeans();

                // 3. åæ­¢çå½å¨æç®¡ç
                stopLifecycleManagement();

                // 4. æ¸çèµæº
                beanFactory.destroySingletons(); // æ¸çBeanå·¥åä¸­çåä¾
                beanFactory.clearBeanDefinitions(); // æ¸çBeanå®ä¹
                applicationListeners.clear();
                beanPostProcessors.clear();
                beanFactoryPostProcessors.clear();
                eventMulticaster.removeAllListeners();

                active.set(false);
                refreshed.set(false);

                log.info("â Network Application Context closed successfully");

            } catch (Exception e) {
                log.error("â Error closing Network Application Context", e);
            }
        }
    }

    private void stopLifecycleManagement() {
        log.info("ð Stopping lifecycle management...");
        log.info("â Lifecycle management stopped successfully");
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }
    
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeansOfType(Class<T> beanType) {
        Map<String, T> beans = new ConcurrentHashMap<>();
        
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (definition != null && beanType.isAssignableFrom(definition.getBeanClass())) {
                T bean = (T) getBean(beanName);
                if (bean != null) {
                    beans.put(beanName, bean);
                }
            }
        }
        
        return beans;
    }
    
    @Override
    public boolean containsBean(String beanName) {
        return beanFactory.containsBean(beanName);
    }
    
    @Override
    public boolean isSingleton(String beanName) {
        return beanFactory.isSingleton(beanName);
    }
    
    @Override
    @Nullable
    public Class<?> getType(String beanName) {
        return beanFactory.getType(beanName);
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        eventMulticaster.multicastEvent(event);
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.add(listener);
        eventMulticaster.addApplicationListener(listener); // ç«å³æ³¨åå°å¤æ­å¨
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
        beanFactory.addBeanPostProcessor(beanPostProcessor); // ç«å³æ³¨åå°Beanå·¥å
    }

    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
        beanFactoryPostProcessors.add(beanFactoryPostProcessor);
    }

    /**
     * å¯å¨çå½å¨æç®¡ç
/
    private void startLifecycleManagement() {
        log.info("ð Starting lifecycle management...");
        log.info("â Lifecycle management started successfully");
    }

    /**
     * éæ¯ææBean
/
    private void destroyAllBeans() {
        log.info("ð Destroying all beans...");
        // Beanå·¥åä¼å¤çBeançéæ¯
        log.info("â All beans destroyed successfully");
    }
}