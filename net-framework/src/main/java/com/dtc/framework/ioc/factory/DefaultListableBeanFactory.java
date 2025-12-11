package com.dtc.framework.ioc.factory;

import com.dtc.framework.ioc.annotation.Inject;
import com.dtc.framework.ioc.annotation.PostConstruct;
import com.dtc.framework.ioc.annotation.PreDestroy;
import com.dtc.framework.ioc.core.BeanDefinition;
import com.dtc.framework.ioc.exception.BeanCreationException;
import com.dtc.framework.ioc.exception.BeansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心 Bean 工厂实现
 * 包含特性：12(循环依赖), 3(生命周期), 2(依赖注入)
 */
public class DefaultListableBeanFactory implements BeanFactory {
    private static final Logger log = LoggerFactory.getLogger(DefaultListableBeanFactory.class);

    // Bean定义 Map
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    
    // 一级缓存：单例池
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    
    // 二级缓存：早期对象（未填充属性，用于解决循环依赖）
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    
    // 三级缓存：单例工厂（用于AOP代理提前生成）
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();
    
    // 扩展处理器
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    // 正在创建中的Bean集合
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    public void addBeanPostProcessor(BeanPostProcessor processor) {
        this.beanPostProcessors.add(processor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return requiredType.cast(getBean(name));
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        // 简化实现：遍历查找。优化方案：维护 type -> names 的索引
        List<String> candidates = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (entry.getValue().getBeanClass() != null && requiredType.isAssignableFrom(entry.getValue().getBeanClass())) {
                candidates.add(entry.getKey());
            }
        }
        
        if (candidates.isEmpty()) {
            // 尝试在单例池中查找（处理手动注册的单例）
            for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
                if (requiredType.isInstance(entry.getValue())) {
                    return requiredType.cast(entry.getValue());
                }
            }
            throw new BeansException("No bean found of type " + requiredType.getName());
        }
        
        if (candidates.size() > 1) {
            // 如果有多个，优先找 Primary
            for (String candidate : candidates) {
                if (beanDefinitionMap.get(candidate).isPrimary()) {
                    return getBean(candidate, requiredType);
                }
            }
            throw new BeansException("Multiple beans found of type " + requiredType.getName() + ": " + candidates);
        }
        
        return getBean(candidates.get(0), requiredType);
    }

    @Override
    public boolean containsBean(String name) {
        return singletonObjects.containsKey(name) || beanDefinitionMap.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        Object singleton = singletonObjects.get(name);
        if (singleton != null) return true;
        
        BeanDefinition bd = beanDefinitionMap.get(name);
        if (bd == null) throw new BeansException("No bean named '" + name + "' available");
        return bd.isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        BeanDefinition bd = beanDefinitionMap.get(name);
        if (bd == null) throw new BeansException("No bean named '" + name + "' available");
        return bd.isPrototype();
    }

    protected Object doGetBean(String beanName) {
        // 1. 尝试从缓存获取
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null) {
            return sharedInstance;
        }

        BeanDefinition bd = beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new BeansException("No bean named '" + beanName + "' available");
        }

        // 2. 创建 Bean
        if (bd.isSingleton()) {
            return getSingleton(beanName, () -> {
                try {
                    return createBean(beanName, bd);
                } catch (Exception e) {
                    throw new BeanCreationException(beanName, "Creation failed", e);
                }
            });
        } else {
            return createBean(beanName, bd);
        }
    }

    // 三级缓存核心逻辑
    protected Object getSingleton(String beanName) {
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null && singletonsCurrentlyInCreation.contains(beanName)) {
            synchronized (singletonObjects) {
                singletonObject = earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        earlySingletonObjects.put(beanName, singletonObject);
                        singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                singletonsCurrentlyInCreation.add(beanName);
                try {
                    singletonObject = singletonFactory.getObject();
                    singletonObjects.put(beanName, singletonObject);
                    singletonFactories.remove(beanName);
                    earlySingletonObjects.remove(beanName);
                } finally {
                    singletonsCurrentlyInCreation.remove(beanName);
                }
            }
            return singletonObject;
        }
    }

    protected Object createBean(String beanName, BeanDefinition bd) {
        if (log.isDebugEnabled()) {
            log.debug("Creating bean '{}'", beanName);
        }
        try {
            // 1. 实例化
            Object instance = createBeanInstance(bd);

            // 2. 提前暴露单例工厂（解决循环依赖 + AOP）
            if (bd.isSingleton()) {
                final Object finalInstance = instance;
                addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, bd, finalInstance));
            }

            // 3. 属性注入
            populateBean(beanName, bd, instance);

            // 4. 初始化
            instance = initializeBean(beanName, instance, bd);

            return instance;
        } catch (Exception e) {
            throw new BeanCreationException(beanName, "Bean creation failed", e);
        }
    }
    
    private void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    // 用于 AOP 的 Hook
    protected Object getEarlyBeanReference(String beanName, BeanDefinition bd, Object bean) {
        Object exposedObject = bean;
        // 这里可以遍历 SmartInstantiationAwareBeanPostProcessor 进行 AOP 代理
        return exposedObject;
    }

    private Object createBeanInstance(BeanDefinition bd) throws Exception {
        // 1. 工厂方法创建 (Support @Bean)
        if (bd.getFactoryMethodName() != null) {
            return createBeanUsingFactoryMethod(bd);
        }

        Class<?> beanClass = bd.getBeanClass();
        if (beanClass == null) {
             throw new BeanCreationException(bd.getBeanClassName(), "Bean class is null");
        }
        
        // 2. 构造器创建
        // 简单实现：优先查找 @Inject 构造器，否则用无参构造器
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                c.setAccessible(true);
                Class<?>[] paramTypes = c.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = getBean(paramTypes[i]); // 递归解析依赖
                }
                return c.newInstance(args);
            }
        }
        
        // fallback to no-arg
        try {
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // 如果没有无参构造函数，且没有@Inject标注的构造函数，且只有一个构造函数，尝试自动注入
             if (constructors.length == 1) {
                Constructor<?> c = constructors[0];
                c.setAccessible(true);
                Class<?>[] paramTypes = c.getParameterTypes();
                 if (paramTypes.length > 0) {
                     Object[] args = new Object[paramTypes.length];
                     for (int i = 0; i < paramTypes.length; i++) {
                         args[i] = getBean(paramTypes[i]);
                     }
                     return c.newInstance(args);
                 } else {
                     return c.newInstance();
                 }
             }
             throw e;
        }
    }

    private Object createBeanUsingFactoryMethod(BeanDefinition bd) throws Exception {
        String factoryBeanName = bd.getFactoryBeanName();
        Object factoryBean = getBean(factoryBeanName);
        
        String factoryMethodName = bd.getFactoryMethodName();
        // 简单实现：只根据方法名查找，不处理重载
        for (java.lang.reflect.Method method : factoryBean.getClass().getDeclaredMethods()) {
            if (method.getName().equals(factoryMethodName)) {
                method.setAccessible(true);
                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = getBean(paramTypes[i]);
                }
                return method.invoke(factoryBean, args);
            }
        }
        throw new BeanCreationException(bd.getBeanClassName(), "Factory method '" + factoryMethodName + "' not found in bean '" + factoryBeanName + "'");
    }

    private void populateBean(String beanName, BeanDefinition bd, Object instance) throws Exception {
        // 字段注入
        Class<?> clazz = bd.getBeanClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object value = getBean(field.getType());
                    field.set(instance, value);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private Object initializeBean(String beanName, Object bean, BeanDefinition bd) {
        Object wrappedBean = bean;
        
        // 1. PostProcessBeforeInitialization
        for (BeanPostProcessor processor : beanPostProcessors) {
            wrappedBean = processor.postProcessBeforeInitialization(wrappedBean, beanName);
            if (wrappedBean == null) return null;
        }

        // 2. init-method
        try {
            invokeInitMethods(beanName, wrappedBean, bd);
        } catch (Exception e) {
            throw new BeanCreationException(beanName, "Invocation of init method failed", e);
        }

        // 3. PostProcessAfterInitialization (AOP 代理通常在这里生成)
        for (BeanPostProcessor processor : beanPostProcessors) {
            wrappedBean = processor.postProcessAfterInitialization(wrappedBean, beanName);
            if (wrappedBean == null) return null;
        }
        
        return wrappedBean;
    }

    private void invokeInitMethods(String beanName, Object bean, BeanDefinition bd) throws Exception {
        // 1. 调用 @PostConstruct 方法
        Class<?> clazz = bean.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        // 2. 调用 init-method
        if (bd.getInitMethodName() != null && !bd.getInitMethodName().isEmpty()) {
            bean.getClass().getMethod(bd.getInitMethodName()).invoke(bean);
        }
    }
    
    public void invokeDestroyMethods(String beanName, Object bean, BeanDefinition bd) throws Exception {
        try {
            // 1. 调用 @PreDestroy 方法
            Class<?> clazz = bean.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PreDestroy.class)) {
                        method.setAccessible(true);
                        method.invoke(bean);
                    }
                }
                clazz = clazz.getSuperclass();
            }
            
            // 2. 调用 destroy-method
            if (bd.getDestroyMethodName() != null && !bd.getDestroyMethodName().isEmpty()) {
                bean.getClass().getMethod(bd.getDestroyMethodName()).invoke(bean);
            }
        } catch (Exception e) {
            log.warn("Error invoking destroy methods for bean '{}'", beanName, e);
        }
    }
    
    public void preInstantiateSingletons() {
        List<String> beanNames = new ArrayList<>(beanDefinitionMap.keySet());
        for (String beanName : beanNames) {
            BeanDefinition bd = beanDefinitionMap.get(beanName);
            if (bd.isSingleton() && !bd.isLazyInit()) { 
                getBean(beanName);
            }
        }
    }
    
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }
    
    public void registerSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.beanDefinitionMap.remove(beanName); // 手动注册的单例不需要定义
            this.earlySingletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
        }
    }

    @FunctionalInterface
    public interface ObjectFactory<T> {
        T getObject();
    }
}

