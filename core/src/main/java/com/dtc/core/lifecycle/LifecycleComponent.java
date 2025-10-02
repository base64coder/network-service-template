package com.dtc.core.lifecycle;

/**
 * 生命周期组件接口
 * 定义组件的生命周期方法
 * 
 * @author Network Service Template
 */
public interface LifecycleComponent {

    /**
     * 启动组件
     * 
     * @throws Exception 启动异常
     */
    void start() throws Exception;

    /**
     * 停止组件
     * 
     * @throws Exception 停止异常
     */
    void stop() throws Exception;
}
