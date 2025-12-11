package com.dtc.framework.context.annotation;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Configuration;

import com.dtc.framework.context.type.AnnotationMetadata;
import com.dtc.framework.context.type.classreading.SimpleMetadataReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ClassScanner {
    
    public static Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        
        // 1. Try reading AOT index
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/dtc-components.index");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        if (line.startsWith(packageName)) {
                            try {
                                classes.add(Class.forName(line));
                            } catch (ClassNotFoundException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // ignore
        }
        
        if (!classes.isEmpty()) {
            return classes;
        }
        
        // 2. Fallback to ASM scanning
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
                try {
                    // 使用 ASM 读取元数据，避免加载类
                    try (FileInputStream is = new FileInputStream(file)) {
                        SimpleMetadataReader reader = new SimpleMetadataReader(is);
                        AnnotationMetadata metadata = reader.getAnnotationMetadata();
                        
                        if (metadata.hasAnnotation(Component.class.getName()) || 
                            metadata.hasAnnotation(Configuration.class.getName())) {
                            
                            String className = metadata.getClassName();
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                } catch (NoClassDefFoundError e) {
                    // ignore
                }
            }
        }
    }
}

