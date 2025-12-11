package com.dtc.framework.test.beans.basic;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.PostConstruct;
import com.dtc.framework.beans.annotation.PreDestroy;

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

