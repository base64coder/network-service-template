package com.dtc.framework.test.beans.complex;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.Inject;

@Component
public class ServiceC {
    @Inject
    private ServiceA a;
    
    public ServiceA getA() { return a; }
}

