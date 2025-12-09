package com.dtc.core.bootstrap.launcher;

/**
 * 启动钩子接口
 * 用于在服务器启动和停止时执行自定义逻辑
 */
public interface StartupHook {
    /**
     * 服务器启动时调用
     */
    void onServerStartup();
    
    /**
     * 服务器停止时调用
     */
    void onServerShutdown();
}

