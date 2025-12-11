package com.dtc.framework.test.module;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.test.module.configprops.Props;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationPropertiesTest {

    @BeforeEach
    public void setup() {
        System.setProperty("app.config.name", "TestApp");
        System.setProperty("app.config.timeout", "5000");
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("app.config.name");
        System.clearProperty("app.config.timeout");
    }

    @Test
    public void testProps() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.module.configprops");
        
        Props props = context.getBean(Props.class);
        assertNotNull(props);
        assertEquals("TestApp", props.name);
        assertEquals(5000, props.timeout);
    }
}
