package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;

/**
 * Network Application Context Interface
 * Combines advantages of Spring ApplicationContext with module-based configuration
 * 
 * @author Network Service Template
 */
public interface NetworkApplicationContext {
    
    /**
     * Get Bean instance by type
     * @param beanType Bean type
     * @return Bean instance
     */
    @Nullable
    <T> T getBean(Class<T> beanType);
    
    /**
     * Get Bean instance by name
     * @param beanName Bean name
     * @return Bean instance
     */
    @Nullable
    Object getBean(String beanName);
    
    /**
     * Get Bean instance by name and type
     * @param beanName Bean name
     * @param beanType Bean type
     * @return Bean instance
     */
    @Nullable
    <T> T getBean(String beanName, Class<T> beanType);
    
    /**
     * Get all Bean instances of specified type
     * @param beanType Bean type
     * @return Bean instance map
     */
    @NotNull
    <T> Map<String, T> getBeansOfType(Class<T> beanType);
    
    /**
     * Check if Bean exists
     * @param beanName Bean name
     * @return Whether exists
     */
    boolean containsBean(String beanName);
    
    /**
     * Check if Bean is singleton
     * @param beanName Bean name
     * @return Whether is singleton
     */
    boolean isSingleton(String beanName);
    
    /**
     * Get Bean type
     * @param beanName Bean name
     * @return Bean type
     */
    @Nullable
    Class<?> getType(String beanName);
    
    /**
     * Get all Bean names
     * @return Bean name array
     */
    @NotNull
    String[] getBeanDefinitionNames();
    
    /**
     * Refresh container
     */
    void refresh();
    
    /**
     * Close container
     */
    void close();
    
    /**
     * Check if container is active
     * @return Whether is active
     */
    boolean isActive();
    
    /**
     * Register Bean definition
     * @param beanName Bean name
     * @param beanClass Bean type
     */
    void registerBean(String beanName, Class<?> beanClass);
    
    /**
     * Register Bean instance
     * @param beanName Bean name
     * @param beanInstance Bean instance
     */
    void registerBean(String beanName, Object beanInstance);
    
    /**
     * Publish application event
     * @param event Application event
     */
    void publishEvent(ApplicationEvent event);
    
    /**
     * Add application listener
     * @param listener Application listener
     */
    void addApplicationListener(ApplicationListener<?> listener);
    
    /**
     * Add Bean post processor
     * @param beanPostProcessor Bean post processor
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    
    /**
     * Add Bean factory post processor
     * @param beanFactoryPostProcessor Bean factory post processor
     */
    void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);
}
