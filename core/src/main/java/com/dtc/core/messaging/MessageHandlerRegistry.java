package com.dtc.core.messaging;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.annotations.web.MessageHandler;
import com.dtc.annotations.web.MqttHandler;
import com.dtc.annotations.web.TcpHandler;
import com.dtc.annotations.web.UdpHandler;
import com.dtc.annotations.web.WebSocketHandler;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.inject.Injector;

/**
 * 消息处理器注册表
 * 扫描并注册消息处理器，支持基于注解的消息路由
 * 
 * @author Network Service Template
 */
public class MessageHandlerRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MessageHandlerRegistry.class);
    
    // 协议类型 -> 路由模式 -> 处理器方法列表（按优先级排序）
    private final Map<String, Map<String, List<HandlerMethod>>> handlers = new ConcurrentHashMap<>();
    
    /**
     * 扫描并注册消息处理器
     * 
     * @param injector Guice注入器
     * @param basePackage 扫描的基础包路径
     */
    public void scanAndRegister(@NotNull Injector injector, @NotNull String basePackage) {
        log.info("Scanning message handlers in package: {}", basePackage);
        
        try {
            List<Class<?>> classes = scanClasses(basePackage);
            
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(MessageHandler.class)) {
                    registerHandlerClass(injector, clazz);
                }
            }
            
            log.info("✅ Registered message handlers for {} protocols", handlers.size());
        } catch (Exception e) {
            log.error("Failed to scan message handlers", e);
        }
    }
    
    /**
     * 注册处理器类
     */
    private void registerHandlerClass(@NotNull Injector injector, @NotNull Class<?> handlerClass) {
        try {
            Object handlerInstance = injector.getInstance(handlerClass);
            if (handlerInstance == null) {
                log.warn("Failed to get instance of handler class: {}", handlerClass.getName());
                return;
            }
            
            Method[] methods = handlerClass.getDeclaredMethods();
            for (Method method : methods) {
                // 注册UDP处理器
                if (method.isAnnotationPresent(UdpHandler.class)) {
                    registerUdpHandler(handlerInstance, method);
                }
                
                // 注册TCP处理器
                if (method.isAnnotationPresent(TcpHandler.class)) {
                    registerTcpHandler(handlerInstance, method);
                }
                
                // 注册WebSocket处理器
                if (method.isAnnotationPresent(com.dtc.annotations.web.WebSocketHandler.class)) {
                    registerWebSocketHandler(handlerInstance, method);
                }
                
                // 注册MQTT处理器
                if (method.isAnnotationPresent(MqttHandler.class)) {
                    registerMqttHandler(handlerInstance, method);
                }
            }
            
            log.debug("Registered handler class: {}", handlerClass.getSimpleName());
        } catch (Exception e) {
            log.error("Failed to register handler class: {}", handlerClass.getName(), e);
        }
    }
    
    /**
     * 注册UDP处理器
     */
    private void registerUdpHandler(@NotNull Object handlerInstance, @NotNull Method method) {
        UdpHandler annotation = method.getAnnotation(UdpHandler.class);
        String route = annotation.value();
        int priority = annotation.priority();
        
        registerHandler("UDP", route, handlerInstance, method, priority);
    }
    
    /**
     * 注册TCP处理器
     */
    private void registerTcpHandler(@NotNull Object handlerInstance, @NotNull Method method) {
        TcpHandler annotation = method.getAnnotation(TcpHandler.class);
        String route = annotation.value();
        int priority = annotation.priority();
        
        registerHandler("TCP", route, handlerInstance, method, priority);
    }
    
    /**
     * 注册WebSocket处理器
     */
    private void registerWebSocketHandler(@NotNull Object handlerInstance, @NotNull Method method) {
        WebSocketHandler annotation = method.getAnnotation(WebSocketHandler.class);
        String route = annotation.value();
        int priority = annotation.priority();
        
        registerHandler("WebSocket", route, handlerInstance, method, priority);
    }
    
    /**
     * 注册MQTT处理器
     */
    private void registerMqttHandler(@NotNull Object handlerInstance, @NotNull Method method) {
        MqttHandler annotation = method.getAnnotation(MqttHandler.class);
        String messageType = annotation.messageType();
        String topic = annotation.topic();
        int priority = annotation.priority();
        
        // MQTT使用消息类型+主题作为路由键
        String route = messageType.isEmpty() ? topic : messageType + ":" + topic;
        registerHandler("MQTT", route, handlerInstance, method, priority);
    }
    
    /**
     * 注册处理器
     */
    private void registerHandler(@NotNull String protocol, @NotNull String route,
                                 @NotNull Object handlerInstance, @NotNull Method method,
                                 int priority) {
        Map<String, List<HandlerMethod>> protocolHandlers = 
            handlers.computeIfAbsent(protocol, k -> new ConcurrentHashMap<>());
        
        String routeKey = route.isEmpty() ? "*" : route;
        List<HandlerMethod> handlerList = 
            protocolHandlers.computeIfAbsent(routeKey, k -> new ArrayList<>());
        
        HandlerMethod handlerMethod = new HandlerMethod(handlerInstance, method, priority);
        handlerList.add(handlerMethod);
        
        // 按优先级排序
        handlerList.sort(Comparator.comparingInt(HandlerMethod::getPriority));
        
        log.debug("Registered {} handler: {} -> {}.{}", protocol, routeKey, 
            handlerInstance.getClass().getSimpleName(), method.getName());
    }
    
    /**
     * 查找处理器
     * 
     * @param protocol 协议类型
     * @param message 消息内容
     * @return 处理器方法，如果未找到则返回null
     */
    @Nullable
    public HandlerMethod findHandler(@NotNull String protocol, @NotNull String message) {
        Map<String, List<HandlerMethod>> protocolHandlers = handlers.get(protocol);
        if (protocolHandlers == null || protocolHandlers.isEmpty()) {
            return null;
        }
        
        // 1. 精确匹配路由
        List<HandlerMethod> exactHandlers = protocolHandlers.get(message);
        if (exactHandlers != null && !exactHandlers.isEmpty()) {
            return exactHandlers.get(0);
        }
        
        // 2. 前缀匹配路由
        for (Map.Entry<String, List<HandlerMethod>> entry : protocolHandlers.entrySet()) {
            String route = entry.getKey();
            if (route.endsWith(":*")) {
                String prefix = route.substring(0, route.length() - 2);
                if (message.startsWith(prefix)) {
                    return entry.getValue().get(0);
                }
            }
        }
        
        // 3. 正则表达式匹配
        for (Map.Entry<String, List<HandlerMethod>> entry : protocolHandlers.entrySet()) {
            String route = entry.getKey();
            if (route.startsWith("^") || route.contains(".*")) {
                try {
                    Pattern pattern = Pattern.compile(route);
                    if (pattern.matcher(message).matches()) {
                        return entry.getValue().get(0);
                    }
                } catch (Exception e) {
                    log.warn("Invalid regex pattern: {}", route);
                }
            }
        }
        
        // 4. 使用默认通配符处理器
        List<HandlerMethod> defaultHandlers = protocolHandlers.get("*");
        if (defaultHandlers != null && !defaultHandlers.isEmpty()) {
            return defaultHandlers.get(0);
        }
        
        return null;
    }
    
    /**
     * 查找MQTT处理器
     */
    @Nullable
    public HandlerMethod findMqttHandler(@NotNull String messageType, @Nullable String topic) {
        Map<String, List<HandlerMethod>> protocolHandlers = handlers.get("MQTT");
        if (protocolHandlers == null || protocolHandlers.isEmpty()) {
            return null;
        }
        
        // 1. 精确匹配消息类型+主题
        if (topic != null && !topic.isEmpty()) {
            String route = messageType + ":" + topic;
            List<HandlerMethod> routeHandlers = protocolHandlers.get(route);
            if (routeHandlers != null && !routeHandlers.isEmpty()) {
                return routeHandlers.get(0);
            }
        }
        
        // 2. 仅匹配消息类型
        List<HandlerMethod> typeHandlers = protocolHandlers.get(messageType);
        if (typeHandlers != null && !typeHandlers.isEmpty()) {
            return typeHandlers.get(0);
        }
        
        // 3. 使用默认通配符处理器
        List<HandlerMethod> defaultHandlerList = protocolHandlers.get("*");
        if (defaultHandlerList != null && !defaultHandlerList.isEmpty()) {
            return defaultHandlerList.get(0);
        }
        
        return null;
    }
    
    /**
     * 扫描类
     */
    @NotNull
    private List<Class<?>> scanClasses(@NotNull String basePackage) {
        // 使用ComponentScanner来扫描类
        return com.dtc.core.web.ComponentScanner.scanClasses(basePackage);
    }
    
    /**
     * 处理器方法
     */
    public static class HandlerMethod {
        private final Object handlerInstance;
        private final Method method;
        private final int priority;
        
        public HandlerMethod(@NotNull Object handlerInstance, @NotNull Method method, int priority) {
            this.handlerInstance = handlerInstance;
            this.method = method;
            this.priority = priority;
            this.method.setAccessible(true);
        }
        
        @NotNull
        public Object getHandlerInstance() {
            return handlerInstance;
        }
        
        @NotNull
        public Method getMethod() {
            return method;
        }
        
        public int getPriority() {
            return priority;
        }
        
        /**
         * 调用处理器方法
         */
        @Nullable
        public Object invoke(@NotNull Object... args) throws Exception {
            return method.invoke(handlerInstance, args);
        }
    }
}
