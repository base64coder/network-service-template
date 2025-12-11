package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;

@Component
class ServiceA {
    @Inject
    ServiceB b;
}

@Component
class ServiceB {
    @Inject
    ServiceA a;
}

