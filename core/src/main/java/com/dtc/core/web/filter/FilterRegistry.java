package com.dtc.core.web.filter;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 过滤器注册表
 * 管理和注册所有过滤器
 * 
 * @author Network Service Template
 */
@Singleton
public class FilterRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(FilterRegistry.class);
    
    private final List<Filter> filters = new CopyOnWriteArrayList<>();
    
    @Inject
    public FilterRegistry() {
        log.info("Filter Registry initialized");
    }
    
    /**
     * 注册过滤器
     * 
     * @param filter 过滤器
     */
    public void registerFilter(@NotNull Filter filter) {
        filters.add(filter);
        // 按优先级排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));
        log.info("Registered filter: {} with order: {}", filter.getName(), filter.getOrder());
    }
    
    /**
     * 移除过滤器
     * 
     * @param filter 过滤器
     * @return 是否移除成功
     */
    public boolean removeFilter(@NotNull Filter filter) {
        boolean removed = filters.remove(filter);
        if (removed) {
            log.info("Removed filter: {}", filter.getName());
        }
        return removed;
    }
    
    /**
     * 获取所有过滤器
     * 
     * @return 过滤器列表（按优先级排序）
     */
    @NotNull
    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }
    
    /**
     * 执行过滤器链
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param targetChain 目标处理器链，在所有过滤器执行完毕后调用
     * @throws Exception 如果在过滤或处理过程中发生错误
     */
    public void doFilter(@NotNull com.dtc.core.network.http.HttpRequestEx request, 
                        @NotNull com.dtc.core.network.http.HttpResponseEx response,
                        @NotNull FilterChain targetChain) throws Exception {
        new DefaultFilterChain(filters, targetChain).doFilter(request, response);
    }
    
    /**
     * 清除所有过滤器
     */
    public void clearFilters() {
        filters.clear();
        log.info("All filters cleared");
    }
    
    /**
     * 获取过滤器数量
     * 
     * @return 过滤器数量
     */
    public int getFilterCount() {
        return filters.size();
    }
}

