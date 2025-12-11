package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;
import com.dtc.core.framework.ioc.context.ApplicationContext;
import com.dtc.core.framework.ioc.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtcIocCircularDependencyTest {

    @Test
    public void testCircularDependency() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        ServiceA a = context.getBean(ServiceA.class);
        ServiceB b = context.getBean(ServiceB.class);
        
        assertNotNull(a);
        assertNotNull(b);
        assertSame(a.b, b);
        assertSame(b.a, a);
    }
}

