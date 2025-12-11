package com.dtc.framework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class HandlerExecutionChain {
    private final Object handler;
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    private int interceptorIndex = -1;

    public HandlerExecutionChain(Object handler) {
        this.handler = handler;
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    public void addInterceptors(List<HandlerInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }
    
    public Object getHandler() {
        return handler;
    }

    public boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            if (!interceptor.preHandle(request, response, handler)) {
                triggerAfterCompletion(request, response, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    public void applyPostHandle(HttpServletRequest request, HttpServletResponse response, Object mv) throws Exception {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).postHandle(request, response, handler, mv);
        }
    }

    public void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex) throws Exception {
        for (int i = this.interceptorIndex; i >= 0; i--) {
            try {
                interceptors.get(i).afterCompletion(request, response, handler, ex);
            } catch (Throwable t) {
                // log error
            }
        }
    }
}

