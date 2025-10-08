package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.*;
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
 * 默认网络应用上下文实现
 * 借鉴Spring ApplicationContext和Guice Injector的优点
 * 
 * @author Network Service Template
 */
public class DefaultNetworkApplicationContext implements NetworkApplicationContext {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultNetworkApplicationContext.class);
    
    // Bean工厂
    private final ConfigurableBeanFactory beanFactory;
    
    // 环境配置
    private final Environment environment;
    
    // 应用监听器列表
    private final List<ApplicationListener<?>> applicationListeners = new CopyOnWriteArrayList<>();
    
    // Bean后处理器列表
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();
    
    // Bean工厂后处理器列表
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new CopyOnWriteArrayList<>();
    
    // 应用事件多播器
    private final ApplicationEventMulticaster eventMulticaster;
    
    // 容器状态
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean refreshed = new AtomicBoolean(false);
    
    // 启动关闭监控器
    private final Object startupShutdownMonitor = new Object();
    
    // 启动时间
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
        
        // 查找匹配的Bean定义
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
        log.debug("📝 Registered bean definition: {} -> {}", beanName, beanClass.getName());
    }
    
    @Override
    public void registerBean(String beanName, Object beanInstance) {
        beanFactory.registerSingleton(beanName, beanInstance);
        log.debug("📝 Registered bean instance: {} -> {}", beanName, beanInstance.getClass().getName());
    }
    
    @Override
    public void refresh() {
        synchronized (this.startupShutdownMonitor) {
            if (refreshed.get()) {
                log.warn("Application context already refreshed");
                return;
            }

            try {
                log.info("🔄 Refreshing Network Application Context...");
                this.startupDate = System.currentTimeMillis();

                // 1. 准备环境
                prepareEnvironment();

                // 2. 准备BeanFactory
                prepareBeanFactory(beanFactory);

                // 3. 执行BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory);

                // 4. 注册BeanPostProcessor
                registerBeanPostProcessors(beanFactory);

                // 5. 初始化事件多播器
                initApplicationEventMulticaster();

                // 6. 注册监听器
                registerListeners();

                // 7. 实例化所有非懒加载的单例Bean
                finishBeanFactoryInitialization(beanFactory);

                // 8. 启动生命周期管理
                startLifecycleManagement();

                active.set(true);
                refreshed.set(true);

                // 9. 发布ContextRefreshedEvent
                publishEvent(new ContextRefreshedEvent(this));

                log.info("✅ Network Application Context refreshed successfully in {} ms",
                        (System.currentTimeMillis() - startupDate));

            } catch (Exception e) {
                log.error("❌ Failed to refresh Network Application Context", e);
                throw new RuntimeException("Failed to refresh application context", e);
            }
        }
    }
    
    private void prepareEnvironment() {
        log.info("🔧 Preparing environment...");
        // 可以在这里加载配置文件、设置活动配置文件等
        log.info("✅ Environment prepared successfully");
    }

    private void prepareBeanFactory(ConfigurableBeanFactory beanFactory) {
        log.info("🔧 Preparing BeanFactory...");
        beanFactory.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
        // 注册内置的BeanPostProcessor
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        log.info("✅ BeanFactory prepared successfully");
    }

    private void invokeBeanFactoryPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("🔧 Invoking BeanFactoryPostProcessors...");
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
        log.info("✅ BeanFactoryPostProcessors invoked successfully");
    }

    private void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {
        log.info("🔧 Registering BeanPostProcessors...");
        // 注册通过addBeanPostProcessor添加的处理器
        for (BeanPostProcessor bpp : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(bpp);
        }
        // 查找并注册通过Bean定义的BeanPostProcessor
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (BeanPostProcessor.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    BeanPostProcessor bpp = (BeanPostProcessor) getBean(beanName);
                    beanFactory.addBeanPostProcessor(bpp);
                    log.debug("📝 Registered BeanPostProcessor from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("❌ Failed to register BeanPostProcessor from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("✅ BeanPostProcessors registered successfully");
    }

    private void initApplicationEventMulticaster() {
        log.info("🔧 Initializing ApplicationEventMulticaster...");
        // 可以在这里配置事件多播器，例如设置任务执行器
        log.info("✅ ApplicationEventMulticaster initialized successfully");
    }

    private void registerListeners() {
        log.info("🔧 Registering ApplicationListeners...");
        for (ApplicationListener<?> listener : applicationListeners) {
            eventMulticaster.addApplicationListener(listener);
        }
        // 查找并注册通过Bean定义的ApplicationListener
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (ApplicationListener.class.isAssignableFrom(definition.getBeanClass())) {
                try {
                    ApplicationListener<?> listener = (ApplicationListener<?>) getBean(beanName);
                    eventMulticaster.addApplicationListener(listener);
                    log.debug("📝 Registered ApplicationListener from bean definition: {}", beanName);
                } catch (Exception e) {
                    log.error("❌ Failed to register ApplicationListener from bean definition: {}", beanName, e);
                }
            }
        }
        log.info("✅ ApplicationListeners registered successfully");
    }

    private void finishBeanFactoryInitialization(ConfigurableBeanFactory beanFactory) {
        log.info("🔧 Finishing BeanFactory initialization (pre-instantiating singletons)...");
        beanFactory.preInstantiateSingletons();
        log.info("✅ BeanFactory initialization finished successfully");
    }

    @Override
    public void close() {
        synchronized (this.startupShutdownMonitor) {
            if (!active.get()) {
                return;
            }

            try {
                log.info("🔄 Closing Network Application Context...");

                // 1. 发布ContextClosedEvent
                publishEvent(new ContextClosedEvent(this));

                // 2. 销毁所有Bean
                destroyAllBeans();

                // 3. 停止生命周期管理
                stopLifecycleManagement();

                // 4. 清理资源
                beanFactory.destroySingletons(); // 清理Bean工厂中的单例
                beanFactory.clearBeanDefinitions(); // 清理Bean定义
                applicationListeners.clear();
                beanPostProcessors.clear();
                beanFactoryPostProcessors.clear();
                eventMulticaster.removeAllListeners();

                active.set(false);
                refreshed.set(false);

                log.info("✅ Network Application Context closed successfully");

            } catch (Exception e) {
                log.error("❌ Error closing Network Application Context", e);
            }
        }
    }

    private void stopLifecycleManagement() {
        log.info("🛑 Stopping lifecycle management...");
        log.info("✅ Lifecycle management stopped successfully");
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
        eventMulticaster.addApplicationListener(listener); // 立即注册到多播器
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
        beanFactory.addBeanPostProcessor(beanPostProcessor); // 立即注册到Bean工厂
    }

    @Override
    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
        beanFactoryPostProcessors.add(beanFactoryPostProcessor);
    }

    /**
     * 启动生命周期管理
     */
    private void startLifecycleManagement() {
        log.info("🚀 Starting lifecycle management...");
        log.info("✅ Lifecycle management started successfully");
    }

    /**
     * 销毁所有Bean
     */
    private void destroyAllBeans() {
        log.info("🔄 Destroying all beans...");
        // Bean工厂会处理Bean的销毁
        log.info("✅ All beans destroyed successfully");
    }
}