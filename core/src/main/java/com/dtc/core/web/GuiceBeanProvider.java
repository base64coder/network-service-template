package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Guice Bean提供者
 * 通过Guice Injector获取Bean实例和类型信息
 * 如果指定了扫描基础包，则通过ComponentScanner扫描并获取所有已注册的Bean实例
 * 
 * @author Network Service Template
 */
public class GuiceBeanProvider implements BeanProvider {

    private static final Logger log = LoggerFactory.getLogger(GuiceBeanProvider.class);

    private final @NotNull Injector injector;
    private @Nullable String scanBasePackage;
    private @Nullable Map<String, Object> cachedBeans;

    public GuiceBeanProvider(@NotNull Injector injector) {
        this.injector = injector;
    }

    public GuiceBeanProvider(@NotNull Injector injector, @NotNull String scanBasePackage) {
        this.injector = injector;
        this.scanBasePackage = scanBasePackage;
    }

    @Override
    @NotNull
    public <T> Map<String, T> getBeansOfType(@NotNull Class<T> beanType) {
        Map<String, T> beans = new HashMap<>();
        
        try {
            // 尝试通过Guice获取该类型的Bean实例
            T bean = injector.getInstance(beanType);
            if (bean != null) {
                beans.put(beanType.getSimpleName(), bean);
            }
        } catch (Exception e) {
            // Guice可能没有绑定该类型的Bean，这是正常的
            log.debug("No binding found for type: {}", beanType.getName());
        }
        
        return beans;
    }

    @Override
    @NotNull
    public Map<String, Object> getAllBeans() {
        // 如果缓存存在则直接使用
        if (cachedBeans != null) {
            return cachedBeans;
        }
        
        // 如果指定了扫描基础包，则通过ComponentScanner扫描并获取所有已注册的Bean实例
        if (scanBasePackage != null && !scanBasePackage.isEmpty()) {
            cachedBeans = ComponentScanner.scanControllersFromInjector(injector, scanBasePackage);
            return cachedBeans;
        }
        
        // 否则返回空map
        log.warn("No scan base package specified. Use setScanBasePackage() or pass it to constructor.");
        return new HashMap<>();
    }

    /**
     * 设置扫描基础包
     */
    public void setScanBasePackage(@NotNull String scanBasePackage) {
        this.scanBasePackage = scanBasePackage;
        this.cachedBeans = null; // 清除缓存
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        this.cachedBeans = null;
    }
}
