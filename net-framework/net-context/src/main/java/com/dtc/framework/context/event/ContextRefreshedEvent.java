package com.dtc.framework.context.event;

import com.dtc.framework.context.ApplicationContext;

public class ContextRefreshedEvent extends ApplicationEvent {
    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

