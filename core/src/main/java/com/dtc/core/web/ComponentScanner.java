package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.Repository;
import com.dtc.annotations.ioc.Service;
import com.dtc.annotations.web.RestController;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组件扫描器
 * 扫描指定包下的所有类，查找并注册@RestController等注解的类
 * 参考Spring的ClassPathScanningCandidateComponentProvider实现
 * 
 * @author Network Service Template
 */
public class ComponentScanner {

    private static final Logger log = LoggerFactory.getLogger(ComponentScanner.class);

    /**
     * 扫描指定包下的所有类
     * 
     * @param basePackage 基础包名
     * @return 类列表
     */
    @NotNull
    public static List<Class<?>> scanClasses(@NotNull String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        
        try {
            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("file".equals(protocol)) {
                    // 文件系统路径
                    File directory = new File(resource.getFile());
                    scanDirectory(directory, basePackage, classes);
                } else if ("jar".equals(protocol)) {
                    // JAR文件路径
                    // TODO: 实现JAR文件路径扫描
                    log.warn("JAR scanning not yet implemented");
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to scan classes in package: {}", basePackage, e);
        }
        
        return classes;
    }

    /**
     * 扫描目录
     */
    private static void scanDirectory(@NotNull File directory, @NotNull String packageName, @NotNull List<Class<?>> classes) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 递归扫描子目录
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                // 加载类
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    log.warn("Failed to load class: {}", className, e);
                }
            }
        }
    }

    /**
     * 通过IoC容器扫描并获取组件实例
     * 扫描@RestController、@Service、@Repository和@Component注解的类
     * 
     * @param injector IoC注入器
     * @param basePackage 基础包名
     * @return 组件实例映射，键为Bean名称，值为Controller、Service、Repository等
     */
    @NotNull
    public static Map<String, Object> scanControllersFromInjector(@NotNull Injector injector, @NotNull String basePackage) {
        Map<String, Object> components = new ConcurrentHashMap<>();
        
        try {
            // 扫描类
            List<Class<?>> classes = scanClasses(basePackage);
            
            for (Class<?> clazz : classes) {
                // 检查是否有@RestController、@Service、@Repository或@Component注解
                // 注意：@Service和@Repository实际上是@Component，所以只需要检查@Component即可
                if (clazz.isAnnotationPresent(RestController.class) ||
                    clazz.isAnnotationPresent(Service.class) ||
                    clazz.isAnnotationPresent(Repository.class) ||
                    clazz.isAnnotationPresent(Component.class)) {
                    try {
                        // 尝试通过IoC容器获取实例
                        Object instance = injector.getInstance(clazz);
                        if (instance != null) {
                            String beanName = toBeanName(clazz.getSimpleName());
                            components.put(beanName, instance);
                            
                            // 根据注解类型记录日志
                            if (clazz.isAnnotationPresent(RestController.class)) {
                                log.debug("Found controller: {} -> {}", beanName, clazz.getName());
                            } else if (clazz.isAnnotationPresent(Service.class)) {
                                log.debug("Found service: {} -> {}", beanName, clazz.getName());
                            } else if (clazz.isAnnotationPresent(Repository.class)) {
                                log.debug("Found repository: {} -> {}", beanName, clazz.getName());
                            } else {
                                log.debug("Found component: {} -> {}", beanName, clazz.getName());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed to get instance of {} from IoC container: {}", clazz.getName(), e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to scan components from Injector", e);
        }
        
        return components;
    }

    /**
     * 转换为Bean名称，首字母小写
     */
    @NotNull
    private static String toBeanName(@NotNull String className) {
        if (className.isEmpty()) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
