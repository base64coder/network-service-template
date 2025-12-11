package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;

@Component
public class ServiceA {
    @Inject
    ServiceB b;
}

