package com.dtc.core.messaging.handler;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.messaging.MessageHandlerRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * 消息处理器初始化器
 * 在应用启动时自动扫描并注册所有消息处理器
 * 
 * @author Network Service Template
 */
@Singleton
public class MessageHandlerInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(MessageHandlerInitializer.class);
    
    private final MessageHandlerRegistry registry;
    private final Injector injector;
    
    @Inject
    public MessageHandlerInitializer(@NotNull MessageHandlerRegistry registry, 
                                     @NotNull Injector injector) {
        this.registry = registry;
        this.injector = injector;
        initialize();
    }
    
    /**
     * 初始化消息处理器
     */
    private void initialize() {
        try {
            // 从系统属性获取扫描包路径，默认为com
            String basePackage = System.getProperty("message.handler.scan.package", "com");
            log.info("Initializing message handlers, scanning package: {}", basePackage);
            
            registry.scanAndRegister(injector, basePackage);
            
            log.info("✅ Message handler initialization completed");
        } catch (Exception e) {
            log.warn("⚠️  Failed to initialize message handlers: {}", e.getMessage());
            log.debug("Message handler initialization error details", e);
        }
    }
    
    /**
     * 获取消息处理器注册表
     */
    @NotNull
    public MessageHandlerRegistry getRegistry() {
        return registry;
    }
}
