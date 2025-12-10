package com.dtc.core.framework.ioc.utils;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Configuration;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassScanner {
    
    public static Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        
        if (resource != null) {
            File directory = new File(resource.getFile());
            scanDirectory(directory, packageName, classes);
        }
        return classes;
    }

    private static void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    // 扫描 @Component 和 @Configuration
                    if (clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Configuration.class)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                } catch (NoClassDefFoundError e) {
                    // ignore dependencies missing
                }
            }
        }
    }
}

