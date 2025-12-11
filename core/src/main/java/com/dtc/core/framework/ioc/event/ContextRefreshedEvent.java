package com.dtc.core.framework.ioc.event;

import com.dtc.core.framework.ioc.context.ApplicationContext;

public class ContextRefreshedEvent extends ApplicationEvent {
    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

