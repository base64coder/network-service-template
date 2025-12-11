package com.dtc.framework.test.module;

import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Configuration;
import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NamedInjectionTest {

    @Test
    public void testNamedInjection() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Config.class, Consumer.class);
        context.refresh();
        
        Consumer consumer = context.getBean(Consumer.class);
        assertNotNull(consumer);
        assertEquals("two", consumer.val);
        
        assertEquals("one", context.getBean("primary"));
        assertEquals("two", context.getBean("secondary"));
    }

    @Configuration
    public static class Config {
        @Bean("primary")
        public String s1() { return "one"; }
        
        @Bean("secondary")
        public String s2() { return "two"; }
    }
    
    @Component
    public static class Consumer {
        @Inject @Named("secondary")
        public String val;
    }
}

