package com.dtc.core.web.interceptor;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 拦截器注册表
 * 管理和注册所有拦截器
 * 
 * @author Network Service Template
 */
@Singleton
public class InterceptorRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(InterceptorRegistry.class);
    
    private final List<HandlerInterceptor> interceptors = new CopyOnWriteArrayList<>();
    
    @Inject
    public InterceptorRegistry() {
        log.info("Interceptor Registry initialized");
    }
    
    /**
     * 注册拦截器
     * 
     * @param interceptor 拦截器
     */
    public void addInterceptor(@NotNull HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        // 按优先级排序
        interceptors.sort(Comparator.comparingInt(HandlerInterceptor::getOrder));
        log.info("Registered interceptor: {} with order: {}", interceptor.getName(), interceptor.getOrder());
    }
    
    /**
     * 移除拦截器
     * 
     * @param interceptor 拦截器
     * @return 是否移除成功
     */
    public boolean removeInterceptor(@NotNull HandlerInterceptor interceptor) {
        boolean removed = interceptors.remove(interceptor);
        if (removed) {
            log.info("Removed interceptor: {}", interceptor.getName());
        }
        return removed;
    }
    
    /**
     * 获取所有拦截器
     * 
     * @return 拦截器列表（按优先级排序）
     */
    @NotNull
    public List<HandlerInterceptor> getInterceptors() {
        return new ArrayList<>(interceptors);
    }
    
    /**
     * 执行前置拦截逻辑
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 目标处理器
     * @return {@code true} 如果执行链应该继续，否则 {@code false}
     * @throws Exception 如果在预处理过程中发生错误
     */
    public boolean applyPreHandle(@NotNull com.dtc.core.network.http.HttpRequestEx request, 
                                  @NotNull com.dtc.core.network.http.HttpResponseEx response, 
                                  @NotNull Object handler) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(request, response, handler)) {
                log.debug("Interceptor {} preHandle returned false, stopping chain.", interceptor.getName());
                return false;
            }
        }
        return true;
    }
    
    /**
     * 执行后置拦截逻辑
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 目标处理器
     * @param modelAndView 处理器返回的模型和视图
     * @throws Exception 如果在后处理过程中发生错误
     */
    public void applyPostHandle(@NotNull com.dtc.core.network.http.HttpRequestEx request, 
                                @NotNull com.dtc.core.network.http.HttpResponseEx response, 
                                @NotNull Object handler,
                                @Nullable Object modelAndView) throws Exception {
        // 逆序执行 postHandle
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).postHandle(request, response, handler, modelAndView);
        }
    }
    
    /**
     * 执行完成后的回调逻辑
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 目标处理器
     * @param ex 处理器执行期间抛出的异常
     * @throws Exception 如果在完成回调过程中发生错误
     */
    public void applyAfterCompletion(@NotNull com.dtc.core.network.http.HttpRequestEx request, 
                                     @NotNull com.dtc.core.network.http.HttpResponseEx response, 
                                     @NotNull Object handler,
                                     @Nullable Exception ex) throws Exception {
        // 逆序执行 afterCompletion
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).afterCompletion(request, response, handler, ex);
        }
    }
    
    /**
     * 清除所有拦截器
     */
    public void clearInterceptors() {
        interceptors.clear();
        log.info("All interceptors cleared");
    }
    
    /**
     * 获取拦截器数量
     * 
     * @return 拦截器数量
     */
    public int getInterceptorCount() {
        return interceptors.size();
    }
}

