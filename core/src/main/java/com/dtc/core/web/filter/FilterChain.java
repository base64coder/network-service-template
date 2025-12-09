package com.dtc.core.web.filter;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * 过滤器链接口
 * 用于管理过滤器的执行顺序
 * 
 * @author Network Service Template
 */
public interface FilterChain {
    
    /**
     * 执行下一个过滤器或目标处理器
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    void doFilter(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response);
}

