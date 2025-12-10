package com.dtc.ioc.core.context;

import com.dtc.ioc.core.impl.DefaultNetworkApplicationContext;
import com.dtc.ioc.core.BeanScanner;
import java.util.List;

/**
 * 基于注解的应用上下文
 * 类似于 AnnotationConfigApplicationContext
 * 
 * @author Network Service Template
 */
public class AnnotationConfigApplicationContext extends DefaultNetworkApplicationContext {

    public AnnotationConfigApplicationContext() {
        super();
    }

    public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
        this();
        register(componentClasses);
        refresh();
    }

    public AnnotationConfigApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }
    
    /**
     * 注册组件类
     * @param componentClasses 组件类数组
     */
    public void register(Class<?>... componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            String beanName = componentClass.getSimpleName();
            registerBean(beanName, componentClass);
        }
    }
    
    /**
     * 扫描包路径
     * @param basePackages 基础包路径数组
     */
    public void scan(String... basePackages) {
        for (String basePackage : basePackages) {
            List<Class<?>> components = BeanScanner.scanComponents(basePackage);
            for (Class<?> componentClass : components) {
                String beanName = componentClass.getSimpleName();
                registerBean(beanName, componentClass);
            }
        }
    }
}
