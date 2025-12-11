package com.dtc.framework.context;

import com.dtc.framework.beans.context.ConditionContext;
import com.dtc.framework.beans.factory.BeanFactory;
import com.dtc.framework.beans.factory.ListableBeanFactory;
import java.util.Map;
import java.lang.reflect.AnnotatedElement;
import com.dtc.framework.beans.module.Module;
import com.dtc.framework.beans.module.support.DefaultBinder;
import com.dtc.framework.beans.env.Environment;
import com.dtc.framework.context.env.StandardEnvironment;
import com.dtc.framework.beans.annotation.*;
import java.util.HashSet;
import com.dtc.framework.beans.factory.config.BeanDefinition;
import com.dtc.framework.beans.factory.config.ConfigurationPropertiesBindingPostProcessor;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 注解驱动的应用上下文
 * 支撑特性 4(自动扫描), 5(配置类)
 */
public class AnnotationConfigApplicationContext implements ApplicationContext {
    private static final Logger log = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);
    private final DefaultListableBeanFactory beanFactory;
    private String[] basePackages;
    private final Set<Module> modules = new HashSet<>();
    private final StandardEnvironment environment;
    private final SimpleEventMulticaster eventMulticaster = new SimpleEventMulticaster();

    public AnnotationConfigApplicationContext() {
        this.environment = new StandardEnvironment();
        this.beanFactory = new DefaultListableBeanFactory();
        this.beanFactory.addEmbeddedValueResolver(environment::resolvePlaceholders);
        this.beanFactory.registerResolvableDependency(ApplicationContext.class, this);
        this.basePackages = new String[0];
    }

    public AnnotationConfigApplicationContext(String... basePackages) {
        this();
        this.basePackages = basePackages; // Reassign
        refresh(); // Refresh automatically
    }

    public AnnotationConfigApplicationContext(Module... modules) {
        this();
        // this.basePackages already set
        for (Module m : modules) {
            this.modules.add(m);
        }
        refresh();
    }
    
    public Environment getEnvironment() {
        return environment;
    }

    public void registerModule(Module module) {
        this.modules.add(module);
    }

    @Override
    public void refresh() {
        log.info("Refreshing DtcIoc ApplicationContext...");
        
        // 0. Install Modules (Guice Compatibility)
        installModules();

        // 1. 扫描包
        scan(basePackages);
        
        // 1.2 注册内部后置处理器
        registerInternalPostProcessors();
        
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
    
    private void installModules() {
        if (modules.isEmpty()) return;
        
        DefaultBinder binder = new DefaultBinder(beanFactory);
        for (Module module : modules) {
            binder.install(module);
        }
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

    private void registerInternalPostProcessors() {
        // ConfigurationPropertiesBindingPostProcessor
        BeanDefinition cpBd = new BeanDefinition();
        cpBd.setBeanClass(ConfigurationPropertiesBindingPostProcessor.class);
        beanFactory.registerBeanDefinition(ConfigurationPropertiesBindingPostProcessor.class.getName(), cpBd);
        
        // AopProxyPostProcessor
        try {
            Class<?> aopClass = Class.forName("com.dtc.framework.aop.framework.AopProxyPostProcessor");
            BeanDefinition aopBd = new BeanDefinition();
            aopBd.setBeanClass(aopClass);
            beanFactory.registerBeanDefinition(aopClass.getName(), aopBd);
        } catch (ClassNotFoundException e) {
            // ignore
        }
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
        return checkConditions(method);
    }

    private void registerBeanPostProcessors() {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            if (bd.getBeanClass() != null && BeanPostProcessor.class.isAssignableFrom(bd.getBeanClass())) {
                Object processor = beanFactory.getBean(beanName);
                if (processor instanceof ConfigurationPropertiesBindingPostProcessor) {
                    ((ConfigurationPropertiesBindingPostProcessor) processor).setEmbeddedValueResolver(environment::resolvePlaceholders);
                }
                beanFactory.addBeanPostProcessor((BeanPostProcessor) processor);
            }
        }
    }

    public void register(Class<?>... componentClasses) {
        for (Class<?> clazz : componentClasses) {
            registerBean(clazz);
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
    
    private class ConditionContextImpl implements ConditionContext {
        @Override
        public BeanFactory getBeanFactory() {
            return beanFactory;
        }

        @Override
        public Environment getEnvironment() {
            return environment;
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }
    }
    
    private boolean shouldRegister(Class<?> clazz) {
        return checkConditions(clazz);
    }
    
    private boolean checkConditions(AnnotatedElement element) {
        ConditionContext context = new ConditionContextImpl();
        
        // 1. Direct @Conditional
        if (element.isAnnotationPresent(Conditional.class)) {
            if (!evaluateCondition(element.getAnnotation(Conditional.class), element, context)) {
                return false;
            }
        }
        
        // 2. Meta-annotations
        for (Annotation ann : element.getAnnotations()) {
            Conditional conditional = ann.annotationType().getAnnotation(Conditional.class);
            if (conditional != null) {
                if (!evaluateCondition(conditional, element, context)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean evaluateCondition(Conditional conditional, AnnotatedElement element, ConditionContext context) {
        try {
            Condition condition = conditional.value().getDeclaredConstructor().newInstance();
            return condition.matches(context, element);
        } catch (Exception e) {
            log.warn("Failed to check condition on {}", element, e);
            return false;
        }
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

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return beanFactory.getBeansOfType(type);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanFactory.getBeanDefinitionCount();
    }
}
