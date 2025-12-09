package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultNetworkApplicationContextTest {

    private DefaultNetworkApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new DefaultNetworkApplicationContext();
    }

    @Test
    void testContextCreation() {
        assertNotNull(context);
        assertFalse(context.isActive());
    }

    @Test
    void testRegisterAndGetBean() {
        context.registerBean("testBean", String.class);
        context.refresh();
        
        Object bean = context.getBean("testBean");
        assertNotNull(bean);
    }

    @Test
    void testGetBeanByType() {
        context.registerBean("testBean", TestService.class);
        context.refresh();
        
        TestService service = context.getBean(TestService.class);
        assertNotNull(service);
    }

    @Test
    void testGetBeansOfType() {
        context.registerBean("bean1", TestService.class);
        context.registerBean("bean2", TestService.class);
        context.refresh();
        
        var beans = context.getBeansOfType(TestService.class);
        assertNotNull(beans);
    }

    @Test
    void testPublishEvent() {
        boolean[] eventReceived = {false};
        context.addApplicationListener(event -> eventReceived[0] = true);
        context.refresh();
        
        context.publishEvent(new TestEvent(context));
        assertTrue(eventReceived[0]);
    }

    @Test
    void testClose() {
        context.registerBean("testBean", String.class);
        context.refresh();
        context.close();
        
        assertFalse(context.isActive());
    }

    @Test
    void testAddBeanPostProcessor() {
        BeanPostProcessor processor = new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                return bean;
            }
        };
        
        context.addBeanPostProcessor(processor);
        assertDoesNotThrow(() -> context.refresh());
    }

    static class TestService {
    }

    static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }
}

