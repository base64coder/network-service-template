package com.dtc.framework.beans.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.beans.*;
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
 * Default Network Application Context Implementation
 * Combines advantages of Spring ApplicationContext and Guice Injector
 * 
 * @author Network Service Template
 */
public class DefaultNetworkApplicationContext implements NetworkApplicationContext {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultNetworkApplicationContext.class);
    
    // Bean Factory
    private final ConfigurableBeanFactory beanFactory;
    
    // Environment Configuration
    private final Environment environment;
    
    // Application Listeners List
    private final List<ApplicationListener<?>> applicationListeners = new CopyOnWriteArrayList<>();
    
    // Bean Post Processors List
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();
    
    // Bean Factory Post Processors List
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new CopyOnWriteArrayList<>();
    
    // Application Event Multicaster
    private final ApplicationEventMulticaster eventMulticaster;
    
    // Container State
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean refreshed = new AtomicBoolean(false);
    
    // Startup/Shutdown Monitor
    private final Object startupShutdownMonitor = new Object();
    
    // Startup Time
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
        
        // Find matching Bean definition
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
        log.debug("Registered bean definition: {} -> {}", beanName, beanClass.getName());
    }
    
    @Override
    public void registerBean(String beanName, Object beanInstance) {
        beanFactory.registerSingleton(beanName, beanInstance);
        log.debug("Registered bean instance: {} -> {}", beanName, beanInstance.getClass().getName());
    }
    
    @Override
    public void refresh() {
        synchronized (this.startupShutdownMonitor) {
            if (refreshed.get()) {
                log.warn("Application context already refreshed");
                return;
            }

            try {
                log.info("Refreshing Network Application Context...");
                this.startupDate = System.currentTimeMillis();

                // 1. Prepare Environment
                prepareEnvironment();

                // 2. Prepare BeanFactory
                prepareBeanFactory(beanFactory);

                // 3. Invoke BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory);

                // 4. Register BeanPostProcessor
                registerBeanPostProcessors(beanFactory);

                // 5. Initialize Application Event Multicaster
                initApplicationEventMulticaster();

                // 6. Register Listeners
                registerListeners();

                // 7. Instantiate all non-lazy singleton beans
                finishBeanFactoryInitialization(beanFactory);

                // 8. Start Lifecycle Management
                startLifecycleManagement();

                active.set(true);
                refreshed.set(true);

                // 9. Publish ContextRefreshedEvent
                publishEvent(new ContextRefreshedEvent(this));

                log.info("Network Application Context refreshed successfully in {} ms",
                        (System.currentTimeMillis() - startupDate));

            } catch (Exception e) {
                log.error("Failed to refresh Network Application Context", e);
                throw new RuntimeException("Failed to refresh application context", e);
            }
        }
    }
    
    private void prepareEnvironment() {
        log.info("Preparing environment...");
        // Can load configuration files, set active profiles here
        log.info("Environment prepared successfully");
    }

    private void prepareBeanFactory(ConfigurableBeanFactory beanFactory) {
        log.info("Preparing BeanFactory...");
        beanFactory.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
        // Register built-in BeanPostProcessors
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        log.info("BeanFactory prepared successfully");
    }

    private void invokeBeanFactoryPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("Invoking BeanFactoryPostProcessors...");
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
        log.info("BeanFactoryPostProcessors invoked successfully");
    }

    private void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("Registering BeanPostProcessors...");
        // Register processors added via addBeanPostProcessor
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        // Find and register BeanPostProcessors from bean definitions
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (BeanPostProcessor.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    BeanPostProcessor bpp = (BeanPostProcessor) getBean(beanName);
                    beanFactory.addBeanPostProcessor(bpp);
                    log.debug("Registered BeanPostProcessor from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("Failed to register BeanPostProcessor from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("BeanPostProcessors registered successfully");
    }

    private void initApplicationEventMulticaster() {
        log.info("Initializing ApplicationEventMulticaster...");
        // Can configure event multicaster here, e.g., set task executor
        log.info("ApplicationEventMulticaster initialized successfully");
    }

    private void registerListeners() {
        log.info("Registering ApplicationListeners...");
        for (ApplicationListener<?> listener : applicationListeners) {
            eventMulticaster.addApplicationListener(listener);
        }
        // Find and register ApplicationListeners from bean definitions
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (ApplicationListener.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    ApplicationListener<?> listener = (ApplicationListener<?>) getBean(beanName);
                    eventMulticaster.addApplicationListener(listener);
                    log.debug("Registered ApplicationListener from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("Failed to register ApplicationListener from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("ApplicationListeners registered successfully");
    }

    private void finishBeanFactoryInitialization(ConfigurableBeanFactory beanFactory) {
        log.info("Finishing BeanFactory initialization (pre-instantiating singletons)...");
        beanFactory.preInstantiateSingletons();
        log.info("BeanFactory initialization finished successfully");
    }

    @Override
    public void close() {
        synchronized (this.startupShutdownMonitor) {
            if (!active.get()) {
                return;
            }

            try {
                log.info("Closing Network Application Context...");

                // 1. Publish ContextClosedEvent
                publishEvent(new ContextClosedEvent(this));

                // 2. Destroy all Beans
                destroyAllBeans();

                // 3. Stop Lifecycle Management
                stopLifecycleManagement();

                // 4. Clean up resources
                beanFactory.destroySingletons(); // Clear singletons in BeanFactory
                beanFactory.clearBeanDefinitions(); // Clear bean definitions
                applicationListeners.clear();
                beanPostProcessors.clear();
                beanFactoryPostProcessors.clear();
                eventMulticaster.removeAllListeners();

                active.set(false);
                refreshed.set(false);

                log.info("Network Application Context closed successfully");

            } catch (Exception e) {
                log.error("Error closing Network Application Context", e);
            }
        }
    }

    private void stopLifecycleManagement() {
        log.info("Stopping lifecycle management...");
        log.info("Lifecycle management stopped successfully");
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
        eventMulticaster.addApplicationListener(listener); // Immediately register to multicaster
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
        beanFactory.addBeanPostProcessor(beanPostProcessor); // Immediately register to BeanFactory
    }

    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
        beanFactoryPostProcessors.add(beanFactoryPostProcessor);
    }

    /**
     * Start lifecycle management
     */
    private void startLifecycleManagement() {
        log.info("Starting lifecycle management...");
        log.info("Lifecycle management started successfully");
    }

    /**
     * Destroy all Beans
     */
    private void destroyAllBeans() {
        log.info("Destroying all beans...");
        // BeanFactory will handle bean destruction
        log.info("All beans destroyed successfully");
    }
}
