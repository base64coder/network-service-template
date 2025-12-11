package com.dtc.framework.context.event;

import com.dtc.framework.context.ApplicationContext;

public class ContextClosedEvent extends ApplicationEvent {
    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
    
    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}

