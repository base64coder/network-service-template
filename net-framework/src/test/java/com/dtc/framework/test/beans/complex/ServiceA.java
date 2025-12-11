package com.dtc.framework.test.beans.complex;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.Inject;

@Component
public class ServiceA {
    @Inject
    private ServiceB b;
    
    public ServiceB getB() { return b; }
}

