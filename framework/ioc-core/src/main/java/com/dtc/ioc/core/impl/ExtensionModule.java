package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.NetworkApplicationContext;

/**
 * 扩展模块
 * 配置扩展系统相关组件
 * 
 * @author Network Service Template
 */
public class ExtensionModule extends AbstractIoCModule {
    
    @Override
    public void configure(@NotNull NetworkApplicationContext context) {
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
    public String getModuleDescription() {
        return "扩展系统模块，提供扩展管理、生命周期管理等功能";
    }
    
    @Override
    @NotNull
    public String[] getDependencies() {
        return new String[]{"NetworkServiceModule"}; // 依赖核心模块
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
