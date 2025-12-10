package com.dtc.core.web.filter;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 默认过滤器链实现
 * 
 * @author Network Service Template
 */
public class DefaultFilterChain implements FilterChain {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultFilterChain.class);
    
    private final List<Filter> filters;
    private final FilterChain targetChain;
    private int currentIndex = 0;
    
    public DefaultFilterChain(@NotNull List<Filter> filters, @NotNull FilterChain targetChain) {
        this.filters = filters;
        this.targetChain = targetChain;
    }
    
    @Override
    public void doFilter(@NotNull HttpRequestEx request, @NotNull HttpResponseEx response) {
        if (currentIndex < filters.size()) {
            Filter filter = filters.get(currentIndex);
            currentIndex++;
            
            boolean continueChain = filter.doFilter(request, response, this);
            if (!continueChain) {
                log.debug("Filter {} interrupted the chain", filter.getName());
                return;
            }
        } else {
            // 所有过滤器执行完毕，执行目标处理器
            targetChain.doFilter(request, response);
        }
    }
}

