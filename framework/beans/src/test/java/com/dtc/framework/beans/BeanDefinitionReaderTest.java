package com.dtc.framework.beans;

import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.Service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanDefinitionReaderTest {

    private BeanDefinitionReader reader;

    @Test
    void testReadBeanDefinition() {
        reader = new BeanDefinitionReader();
        
        BeanDefinition definition = reader.readBeanDefinition(TestComponent.class);
        assertNotNull(definition);
        assertEquals("testComponent", definition.getBeanName());
    }

    @Test
    void testReadBeanDefinitionWithService() {
        reader = new BeanDefinitionReader();
        
        BeanDefinition definition = reader.readBeanDefinition(TestService.class);
        assertNotNull(definition);
    }

    @Component("testComponent")
    static class TestComponent {
    }

    @Service
    static class TestService {
    }
}

