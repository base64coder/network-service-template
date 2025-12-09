package com.dtc.ioc.core;

import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.Service;
import com.dtc.ioc.core.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IoCContainerTest {

    @Test
    public void testBasicBeanRegistration() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SimpleService.class);
        context.refresh();

        SimpleService service = context.getBean(SimpleService.class);
        assertNotNull(service);
    }

    @Test
    public void testComponentScan() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.ioc.core.testpkg");
        // context.refresh() is called in constructor
        
        // Assert beans found
        // Note: We need a test package structure for this.
    }
    
    @Service
    public static class SimpleService {
        public String sayHello() { return "Hello"; }
    }
}

