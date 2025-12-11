package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.PostConstruct;
import com.dtc.core.framework.ioc.annotation.PreDestroy;

@Component
public class LifecycleBean {
    private boolean initialized = false;
    private boolean destroyed = false;
    
    @PostConstruct
    public void init() {
        initialized = true;
    }
    
    @PreDestroy
    public void destroy() {
        destroyed = true;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
}

