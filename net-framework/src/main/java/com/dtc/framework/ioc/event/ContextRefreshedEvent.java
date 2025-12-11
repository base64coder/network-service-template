package com.dtc.framework.ioc.event;

import com.dtc.framework.ioc.context.ApplicationContext;

public class ContextRefreshedEvent extends ApplicationEvent {
    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

