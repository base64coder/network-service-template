package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.NetApplicationContext;

/**
 * 扩展模块（自定义IOC实现版本 - 暂时不使用）
 * 配置扩展系统相关组件
 * 
 * 注意：此模块是自定义IOC容器的实现版本，当前框架暂时使用 Google Guice。
 * 请使用 core/src/main/java/com/dtc/core/bootstrap/ioc/ExtensionModule.java（Guice版本）
 * 
 * ExtensionModule 是处理扩展协议的核心模块，功能本身非常重要，不能被弃用。
 * 当自定义IOC容器完全实现后，可以使用此版本替换 Guice 版本。
 * 
 * @author Network Service Template
 */
public class ExtensionModule extends AbstractNetModule {
    
    @Override
    public void configure(@NotNull NetApplicationContext context) {
        // 注册扩展管理组件
        bind(context, "extensionManager", ExtensionManager.class);
        bind(context, "extensionBootstrap", ExtensionBootstrap.class);
        bind(context, "extensionLifecycleHandler", ExtensionLifecycleHandler.class);
        
        // 注册扩展依赖
        bind(context, "httpExtension", HttpExtension.class);
        bind(context, "mqttExtension", MqttExtension.class);
        bind(context, "tcpExtension", TcpExtension.class);
        bind(context, "websocketExtension", WebSocketExtension.class);
    }
    
    @Override
    @NotNull
    public String getModuleName() {
        return "ExtensionModule";
    }
    
    @Override
    @NotNull
    public String getModuleVersion() {
        return "1.0.0";
    }
    
    @Override
    @NotNull
    public String getModuleDescription() {
        return "扩展系统模块，提供扩展管理、生命周期管理等功能";
    }
    
    @Override
    @NotNull
    public String[] getDependencies() {
        return new String[]{"NetServiceModule"}; // 依赖核心模块
    }
    
    // 模拟的类定义（实际项目中这些类应该存在）
    public static class ExtensionManager {}
    public static class ExtensionBootstrap {}
    public static class ExtensionLifecycleHandler {}
    public static class HttpExtension {}
    public static class MqttExtension {}
    public static class TcpExtension {}
    public static class WebSocketExtension {}
}
