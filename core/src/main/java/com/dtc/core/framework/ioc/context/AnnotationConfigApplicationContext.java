package com.dtc.core.framework.ioc.context;

import com.dtc.core.framework.ioc.annotation.Bean;
import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Configuration;
import com.dtc.core.framework.ioc.annotation.Scope;
import com.dtc.core.framework.ioc.exception.BeansException;
import com.dtc.core.framework.ioc.factory.BeanPostProcessor;
import com.dtc.core.framework.ioc.factory.DefaultListableBeanFactory;
import com.dtc.core.framework.ioc.model.BeanDefinition;
import com.dtc.core.framework.ioc.utils.ClassScanner;
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
        
        log.info("DtcIoc ApplicationContext refreshed successfully.");
    }
    
    @Override
    public void close() {
        log.info("Closing DtcIoc ApplicationContext...");
        // 销毁 Bean 逻辑
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
