package com.dtc.framework.test.beans.complex;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Inject;

@Component
public class ServiceC {
    @Inject
    private ServiceA a;
    
    public ServiceA getA() { return a; }
}

