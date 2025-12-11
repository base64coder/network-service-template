package com.dtc.framework.test.beans.complex;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.Inject;

@Component
public class ServiceB {
    @Inject
    private ServiceC c;
    
    public ServiceC getC() { return c; }
}

