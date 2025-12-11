package com.dtc.framework.test.beans.performance;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.Inject;
import com.dtc.framework.ioc.annotation.Scope;

@Component
@Scope("prototype")
public class PrototypeService {
    @Inject
    public SingletonService singletonService;
    
    public void doSomething() {
        singletonService.doSomething();
    }
}

