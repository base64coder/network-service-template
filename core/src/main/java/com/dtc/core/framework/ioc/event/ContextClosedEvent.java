package com.dtc.core.framework.ioc.event;

import com.dtc.core.framework.ioc.context.ApplicationContext;

public class ContextClosedEvent extends ApplicationEvent {
    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

