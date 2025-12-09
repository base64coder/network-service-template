package com.dtc.core.web.interceptor;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;
import com.dtc.core.web.HandlerMethod;

/**
 * 处理器拦截器接口
 * 用于在处理器执行前后执行自定义逻辑
 * 借鉴 Spring WebMVC 的 HandlerInterceptor 设计
 * 
 * @author Network Service Template
 */
public interface HandlerInterceptor {
    
    /**
     * 在处理器执行前调用
     * 如果返回 false，则中断执行链
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器对象（可能是 HandlerMethod 或其他类型）
     * @return 是否继续执行，true 继续，false 中断
     * @throws Exception 异常
     */
    default boolean preHandle(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response, 
                              @NotNull Object handler) throws Exception {
        return true;
    }
    
    /**
     * 在处理器执行后、视图渲染前调用
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器对象（可能是 HandlerMethod 或其他类型）
     * @param result 处理结果
     * @throws Exception 异常
     */
    default void postHandle(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response,
                           @NotNull Object handler, @Nullable Object result) throws Exception {
        // 默认实现为空
    }
    
    /**
     * 在请求处理完成后调用（包括视图渲染）
     * 无论是否发生异常都会调用
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器对象（可能是 HandlerMethod 或其他类型）
     * @param ex 异常，如果没有异常则为 null
     * @throws Exception 异常
     */
    default void afterCompletion(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response,
                                 @NotNull Object handler, @Nullable Exception ex) throws Exception {
        // 默认实现为空
    }
    
    /**
     * 获取拦截器名称
     * 
     * @return 拦截器名称
     */
    @NotNull
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取拦截器优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }
}

