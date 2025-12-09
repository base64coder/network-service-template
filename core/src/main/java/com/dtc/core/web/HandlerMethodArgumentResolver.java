package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;

import java.lang.reflect.Parameter;

/**
 * 处理方法参数解析器接口
 * 负责将HTTP请求参数解析为方法参数值
 * 参考Spring的HandlerMethodArgumentResolver实现
 * 
 * @author Network Service Template
 */
public interface HandlerMethodArgumentResolver {

    /**
     * 检查是否支持该参数类型
     * 
     * @param parameter 方法参数
     * @return 是否支持
     */
    boolean supportsParameter(@NotNull Parameter parameter);

    /**
     * 解析参数值
     * 
     * @param parameter 方法参数
     * @param request HTTP请求
     * @return 解析后的参数值
     * @throws Exception 解析异常
     */
    @Nullable
    Object resolveArgument(@NotNull Parameter parameter, @NotNull HttpRequestEx request) throws Exception;
}
