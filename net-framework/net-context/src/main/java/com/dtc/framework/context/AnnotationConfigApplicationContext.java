package com.dtc.framework.context;

import com.dtc.framework.beans.annotation.*;
import com.dtc.framework.beans.factory.config.BeanDefinition;
import com.dtc.framework.context.event.ContextClosedEvent;
import com.dtc.framework.context.event.ContextRefreshedEvent;
import com.dtc.framework.context.event.SimpleEventMulticaster;
import com.dtc.framework.context.event.ApplicationListener;
import com.dtc.framework.beans.exception.BeansException;
import com.dtc.framework.beans.factory.BeanPostProcessor;
import com.dtc.framework.beans.factory.support.DefaultListableBeanFactory;
import com.dtc.framework.context.annotation.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 注解驱动的应用上下文
 * 支撑特性 4(自动扫描), 5(配置类)
 */
public class AnnotationConfigApplicationContext implements ApplicationContext {
    private static final Logger log = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);
    private final DefaultListableBeanFactory beanFactory;
    private final String[] basePackages;
    private final SimpleEventMulticaster eventMulticaster = new SimpleEventMulticaster();

    public AnnotationConfigApplicationContext(String... basePackages) {
        this.beanFactory = new DefaultListableBeanFactory();
        this.basePackages = basePackages;
        refresh();
    }

    @Override
    public void refresh() {
        log.info("Refreshing DtcIoc ApplicationContext...");
        
        // 1. 扫描包
        scan(basePackages);
        
        // 1.5 处理 @Configuration 类中的 @Bean 方法
        processConfigurationClasses();

        // 2. 注册并实例化所有的 BeanPostProcessor
        registerBeanPostProcessors();
        
        // 3. 实例化所有非懒加载单例
        beanFactory.preInstantiateSingletons();
        
        // 4. 注册事件监听器
        registerEventListeners();
        
        // 5. 发布ContextRefreshedEvent
        eventMulticaster.multicastEvent(new ContextRefreshedEvent(this));
        
        log.info("DtcIoc ApplicationContext refreshed successfully.");
    }
    
    private void registerEventListeners() {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            if (bd != null && bd.getBeanClass() != null) {
                // 检查是否实现了ApplicationListener接口
                if (ApplicationListener.class.isAssignableFrom(bd.getBeanClass())) {
                    try {
                        Object listener = beanFactory.getBean(beanName);
                        eventMulticaster.addListener((ApplicationListener<?>) listener);
                    } catch (Exception e) {
                        log.warn("Failed to register event listener: {}", beanName, e);
                    }
                }
            }
        }
    }
    
    @Override
    public void close() {
        log.info("Closing DtcIoc ApplicationContext...");
        
        // 发布ContextClosedEvent
        eventMulticaster.multicastEvent(new ContextClosedEvent(this));
        
        // 销毁所有单例Bean
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = beanFactory.getBean(beanName);
                BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                if (bd != null && bd.isSingleton()) {
                    ((DefaultListableBeanFactory) beanFactory).invokeDestroyMethods(beanName, bean, bd);
                }
            } catch (Exception e) {
                log.warn("Error destroying bean '{}'", beanName, e);
            }
        }
    }

    @Override
    public String getApplicationName() {
        return "DtcApplication";
    }

    private void processConfigurationClasses() {
        // 获取当前所有已注册的BeanDefinition名称（此时主要是扫描到的类）
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            // 确保已经设置了 beanClass (扫描来的都有，但手动注册的可能没有)
            if (bd.getBeanClass() != null && bd.getBeanClass().isAnnotationPresent(Configuration.class)) {
                parseConfigurationClass(bd.getBeanClass(), beanName);
            }
        }
    }

    private void parseConfigurationClass(Class<?> configClass, String configBeanName) {
        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                // 检查条件装配
                if (!shouldRegisterMethod(method)) {
                    log.debug("Skipping @Bean method {} due to condition", method.getName());
                    continue;
                }
                
                Bean beanAnno = method.getAnnotation(Bean.class);
                String methodBeanName = method.getName();
                if (beanAnno.value().length > 0) {
                    methodBeanName = beanAnno.value()[0];
                }

                BeanDefinition methodBd = new BeanDefinition();
                methodBd.setBeanClass(method.getReturnType());
                methodBd.setFactoryBeanName(configBeanName);
                methodBd.setFactoryMethodName(method.getName());
                
                if (!beanAnno.initMethod().isEmpty()) {
                    methodBd.setInitMethodName(beanAnno.initMethod());
                }
                
                if (!beanAnno.destroyMethod().isEmpty()) {
                    methodBd.setDestroyMethodName(beanAnno.destroyMethod());
                }

                if (method.isAnnotationPresent(Scope.class)) {
                    methodBd.setScope(method.getAnnotation(Scope.class).value());
                }

                // 注册 @Bean 定义
                beanFactory.registerBeanDefinition(methodBeanName, methodBd);
            }
        }
    }
    
    private boolean shouldRegisterMethod(Method method) {
        // 检查 @Conditional
        if (method.isAnnotationPresent(Conditional.class)) {
            Conditional conditional = method.getAnnotation(Conditional.class);
            try {
                Condition condition = conditional.value().getDeclaredConstructor().newInstance();
                return condition.matches();
            } catch (Exception e) {
                log.warn("Failed to instantiate condition for method {}", method.getName(), e);
                return false;
            }
        }
        
        // 检查 @ConditionalOnClass
        if (method.isAnnotationPresent(ConditionalOnClass.class)) {
            ConditionalOnClass onClass = method.getAnnotation(ConditionalOnClass.class);
            OnClassCondition condition = new OnClassCondition(onClass.value());
            if (!condition.matches()) {
                return false;
            }
        }
        
        // 检查 @ConditionalOnMissingBean
        if (method.isAnnotationPresent(ConditionalOnMissingBean.class)) {
            ConditionalOnMissingBean onMissing = method.getAnnotation(ConditionalOnMissingBean.class);
            OnMissingBeanCondition condition = new OnMissingBeanCondition(onMissing.value(), onMissing.name());
            condition.setBeanFactory(beanFactory);
            if (!condition.matches()) {
                return false;
            }
        }
        
        return true;
    }

    private void registerBeanPostProcessors() {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            if (bd.getBeanClass() != null && BeanPostProcessor.class.isAssignableFrom(bd.getBeanClass())) {
                Object processor = beanFactory.getBean(beanName);
                beanFactory.addBeanPostProcessor((BeanPostProcessor) processor);
            }
        }
    }

    private void scan(String[] basePackages) {
        for (String pkg : basePackages) {
            Set<Class<?>> classes = ClassScanner.scanPackage(pkg);
            for (Class<?> clazz : classes) {
                registerBean(clazz);
            }
        }
    }
    
    private void registerBean(Class<?> clazz) {
        // 检查条件装配
        if (!shouldRegister(clazz)) {
            log.debug("Skipping bean registration for {} due to condition", clazz.getName());
            return;
        }
        
        BeanDefinition bd = new BeanDefinition();
        bd.setBeanClass(clazz);
        
        if (clazz.isAnnotationPresent(Scope.class)) {
            bd.setScope(clazz.getAnnotation(Scope.class).value());
        }
        
        // 处理 @Component("name")
        String beanName = clazz.getSimpleName(); 
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            beanName = component.value();
        } else {
            // Configuration 也是 Component
            Configuration config = clazz.getAnnotation(Configuration.class);
            if (config != null && !config.value().isEmpty()) {
                beanName = config.value();
            } else {
                beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
            }
        }
        
        // 简单处理：如果已存在，覆盖还是忽略？目前是覆盖
        beanFactory.registerBeanDefinition(beanName, bd);
    }
    
    private boolean shouldRegister(Class<?> clazz) {
        // 检查 @Conditional
        if (clazz.isAnnotationPresent(Conditional.class)) {
            Conditional conditional = clazz.getAnnotation(Conditional.class);
            try {
                Condition condition = conditional.value().getDeclaredConstructor().newInstance();
                return condition.matches();
            } catch (Exception e) {
                log.warn("Failed to instantiate condition for {}", clazz.getName(), e);
                return false;
            }
        }
        
        // 检查 @ConditionalOnClass
        if (clazz.isAnnotationPresent(ConditionalOnClass.class)) {
            ConditionalOnClass onClass = clazz.getAnnotation(ConditionalOnClass.class);
            OnClassCondition condition = new OnClassCondition(onClass.value());
            if (!condition.matches()) {
                return false;
            }
        }
        
        // 检查 @ConditionalOnMissingBean
        if (clazz.isAnnotationPresent(ConditionalOnMissingBean.class)) {
            ConditionalOnMissingBean onMissing = clazz.getAnnotation(ConditionalOnMissingBean.class);
            OnMissingBeanCondition condition = new OnMissingBeanCondition(onMissing.value(), onMissing.name());
            condition.setBeanFactory(beanFactory);
            if (!condition.matches()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return beanFactory.getBean(name, requiredType);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        return beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        return beanFactory.isPrototype(name);
    }
}
