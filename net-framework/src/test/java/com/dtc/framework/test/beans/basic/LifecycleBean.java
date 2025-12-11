package com.dtc.framework.test.beans.basic;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.PostConstruct;
import com.dtc.framework.ioc.annotation.PreDestroy;

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

