package com.dtc.framework.test.beans.performance;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.beans.annotation.Scope;

@Component
@Scope("prototype")
public class PrototypeService {
    @Inject
    public SingletonService singletonService;
    
    public void doSomething() {
        singletonService.doSomething();
    }
}

