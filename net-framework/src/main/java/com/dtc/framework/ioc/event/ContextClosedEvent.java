package com.dtc.framework.ioc.event;

import com.dtc.framework.ioc.context.ApplicationContext;

public class ContextClosedEvent extends ApplicationEvent {
    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

