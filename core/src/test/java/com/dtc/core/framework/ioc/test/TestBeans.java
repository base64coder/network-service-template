package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;
import com.dtc.core.framework.ioc.annotation.PostConstruct;
import com.dtc.core.framework.ioc.annotation.PreDestroy;
import com.dtc.core.framework.ioc.annotation.Scope;

@Component
class UserRepository {}

@Component
class UserService {
    @Inject
    UserRepository userRepository;
}

@Component
@Scope("prototype")
class PrototypeBean {}

@Component
class LifecycleBean {
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

