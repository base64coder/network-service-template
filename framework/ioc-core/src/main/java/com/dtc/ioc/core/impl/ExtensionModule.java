package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.NetworkApplicationContext;

/**
     * æ©å±æ¨¡å
éç½®æ©å±ç³»ç»ç¸å³ç»ä»¶
@author Network Service Template
/
public class ExtensionModule extends AbstractIoCModule {
    
    @Override
    public void configure(@NotNull NetworkApplicationContext context) {
        // æ³¨åæ©å±ç®¡çç»ä»¶
        bind(context, "extensionManager", ExtensionManager.class);
        bind(context, "extensionBootstrap", ExtensionBootstrap.class);
        bind(context, "extensionLifecycleHandler", ExtensionLifecycleHandler.class);
        
        // æ³¨åæ©å±ä¾èµ
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
        return "æ©å±ç³»ç»æ¨¡åï¼æä¾æ©å±ç®¡çãçå½å¨æç®¡çç­åè½";
    }
    
    @Override
    @NotNull
    public String[] getDependencies() {
        return new String[]{"NetworkServiceModule"}; // ä¾èµæ ¸å¿æ¨¡å
    }
    
    // æ¨¡æçç±»å®ä¹ï¼å®éé¡¹ç®ä¸­è¿äºç±»åºè¯¥å­å¨ï¼
    public static class ExtensionManager {}
    public static class ExtensionBootstrap {}
    public static class ExtensionLifecycleHandler {}
    public static class HttpExtension {}
    public static class MqttExtension {}
    public static class TcpExtension {}
    public static class WebSocketExtension {}
}
