package com.dtc.framework.test.beans.complex;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Inject;

@Component
public class ServiceB {
    @Inject
    private ServiceC c;
    
    public ServiceC getC() { return c; }
}

