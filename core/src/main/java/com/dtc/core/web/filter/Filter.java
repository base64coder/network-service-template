package com.dtc.core.web.filter;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * Filter 接口
 * 用于在请求处理前后执行自定义逻辑
 * 借鉴 Spring Framework 的 Filter 设计
 * 
 * @author Network Service Template
 */
public interface Filter {
    
    /**
     * 初始化过滤器
     * 
     * @param filterConfig 过滤器配置
     */
    default void init(FilterConfig filterConfig) {
        // 默认实现为空
    }
    
    /**
     * 执行过滤逻辑
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param chain 过滤器链
     * @return 是否继续处理链，如果返回 false 则中断处理
     */
    boolean doFilter(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response, @NotNull FilterChain chain);
    
    /**
     * 销毁过滤器
     */
    default void destroy() {
        // 默认实现为空
    }
    
    /**
     * 获取过滤器名称
     * 
     * @return 过滤器名称
     */
    @NotNull
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取过滤器优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }
}

