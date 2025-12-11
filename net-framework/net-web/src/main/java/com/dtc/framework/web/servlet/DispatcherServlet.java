package com.dtc.framework.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dtc.framework.context.ApplicationContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    private ApplicationContext applicationContext;
    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private List<HandlerInterceptor> interceptors = new ArrayList<>();

    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void init() throws ServletException {
        initStrategies(applicationContext);
    }

    protected void initStrategies(ApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initInterceptors(context);
    }

    private void initHandlerMappings(ApplicationContext context) {
        Map<String, HandlerMapping> matchingBeans = 
            context.getBeansOfType(HandlerMapping.class);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings.addAll(matchingBeans.values());
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        Map<String, HandlerAdapter> matchingBeans = 
            context.getBeansOfType(HandlerAdapter.class);
        if (!matchingBeans.isEmpty()) {
            this.handlerAdapters.addAll(matchingBeans.values());
        }
    }

    private void initInterceptors(ApplicationContext context) {
        Map<String, HandlerInterceptor> matchingBeans = 
            context.getBeansOfType(HandlerInterceptor.class);
        if (!matchingBeans.isEmpty()) {
            this.interceptors.addAll(matchingBeans.values());
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            throw new ServletException("Request processing failed", e);
        }
    }

    protected void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Object handler = getHandler(req);
        if (handler == null) {
            noHandlerFound(req, resp);
            return;
        }

        HandlerExecutionChain mappedHandler = new HandlerExecutionChain(handler);
        mappedHandler.addInterceptors(this.interceptors);

        HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
        
        if (!mappedHandler.applyPreHandle(req, resp)) {
            return;
        }

        try {
            Object mv = ha.handle(req, resp, mappedHandler.getHandler());
            mappedHandler.applyPostHandle(req, resp, mv);
            mappedHandler.triggerAfterCompletion(req, resp, null);
        } catch (Exception ex) {
            mappedHandler.triggerAfterCompletion(req, resp, ex);
            throw ex;
        }
    }

    private Object getHandler(HttpServletRequest req) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            Object handler = hm.getHandler(req);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    private HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler + "]");
    }

    private void noHandlerFound(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}

