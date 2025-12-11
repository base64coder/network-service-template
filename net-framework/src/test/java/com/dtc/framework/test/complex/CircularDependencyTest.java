package com.dtc.framework.test.complex;

import com.dtc.framework.ioc.context.AnnotationConfigApplicationContext;
import com.dtc.framework.ioc.context.ApplicationContext;
import com.dtc.framework.test.beans.complex.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircularDependencyTest {

    @Test
    public void testComplexCircularDependency() {
        // A -> B -> C -> A
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.complex");
        
        ServiceA a = context.getBean(ServiceA.class);
        ServiceB b = context.getBean(ServiceB.class);
        ServiceC c = context.getBean(ServiceC.class);
        
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);
        
        assertSame(a.getB(), b);
        assertSame(b.getC(), c);
        assertSame(c.getA(), a);
    }
}

