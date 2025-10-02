package com.dtc.api;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.parameter.ExtensionStartInput;
import com.dtc.api.parameter.ExtensionStartOutput;
import com.dtc.api.parameter.ExtensionStopInput;
import com.dtc.api.parameter.ExtensionStopOutput;

/**
 * 扩展主接口，所有网络协议扩展都必须实现此接口
 * 
 * @author Network Service Template
 */
public interface ExtensionMain {

    /**
     * 扩展启动时调用
     * 
     * @param input  启动输入参数
     * @param output 启动输出参数
     */
    void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output);

    /**
     * 扩展停止时调用
     * 
     * @param input  停止输入参数
     * @param output 停止输出参数
     */
    void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output);
}
