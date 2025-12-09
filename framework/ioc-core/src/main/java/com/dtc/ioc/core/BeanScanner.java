package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.Repository;
import com.dtc.annotations.ioc.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Bean 扫描器
 * 扫描指定包下的所有组件类并注册为 Bean
 * 借鉴 Spring 的 ComponentScan 机制
 * 
 * @author Network Service Template
 */
public class BeanScanner {
    
    private static final Logger log = LoggerFactory.getLogger(BeanScanner.class);
    
    /**
     * 扫描指定包下的所有组件类
     * 
     * @param basePackage 基础包名
     * @return 组件类列表
     */
    @NotNull
    public static List<Class<?>> scanComponents(@NotNull String basePackage) {
        List<Class<?>> components = new ArrayList<>();
        
        try {
            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("file".equals(protocol)) {
                    File directory = new File(resource.getFile());
                    scanDirectory(directory, basePackage, components);
                } else if ("jar".equals(protocol)) {
                    // TODO: 实现JAR文件路径扫描
                    log.warn("JAR scanning not yet implemented");
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to scan components in package: {}", basePackage, e);
        }
        
        return components;
    }
    
    /**
     * 扫描目录
     */
    private static void scanDirectory(@NotNull File directory, @NotNull String packageName, 
                                      @NotNull List<Class<?>> components) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), components);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (isComponent(clazz)) {
                        components.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Failed to load class: {}", className, e);
                }
            }
        }
    }
    
    /**
     * 检查类是否为组件
     */
    private static boolean isComponent(@NotNull Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
               clazz.isAnnotationPresent(Service.class) ||
               clazz.isAnnotationPresent(Repository.class);
    }
}

