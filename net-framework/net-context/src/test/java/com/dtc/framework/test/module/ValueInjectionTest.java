package com.dtc.framework.test.module;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Value;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValueInjectionTest {

    @BeforeEach
    public void setup() {
        System.setProperty("test.app.name", "DtcApp");
        System.setProperty("test.app.port", "8080");
        System.setProperty("test.app.enable", "true");
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("test.app.name");
        System.clearProperty("test.app.port");
        System.clearProperty("test.app.enable");
    }

    @Test
    public void testValueInjection() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.module");
        
        AppConfig config = context.getBean(AppConfig.class);
        assertNotNull(config);
        assertEquals("DtcApp", config.appName);
        assertEquals(8080, config.port);
        assertTrue(config.enable);
    }

    @Component
    public static class AppConfig {
        @Value("${test.app.name}")
        public String appName;
        
        @Value("${test.app.port}")
        public int port;
        
        @Value("${test.app.enable}")
        public boolean enable;
    }
}

