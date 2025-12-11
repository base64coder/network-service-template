package com.dtc.framework.test.performance;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.test.beans.performance.PrototypeService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class IoCPerformanceTest {

    private static final int BEAN_COUNT = 1000;
    private static final int INJECTION_COUNT = 10000;

    @Test
    public void testStartupTime() {
        long startTime = System.nanoTime();
        
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.performance");
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        System.out.println("Startup time: " + duration + " ms");
    }

    @Test
    public void testPrototypeInjectionPerformance() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.performance");
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < INJECTION_COUNT; i++) {
            PrototypeService service = context.getBean(PrototypeService.class);
            // 简单使用一下
            service.doSomething();
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        System.out.println("Prototype injection (" + INJECTION_COUNT + " times): " + duration + " ms");
    }
}

