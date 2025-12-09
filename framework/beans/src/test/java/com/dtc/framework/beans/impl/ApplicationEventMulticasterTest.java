package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.ApplicationEvent;
import com.dtc.framework.beans.ApplicationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationEventMulticasterTest {

    private SimpleApplicationEventMulticaster multicaster;
    private boolean eventReceived = false;

    @BeforeEach
    void setUp() {
        multicaster = new SimpleApplicationEventMulticaster();
    }

    @Test
    void testAddApplicationListener() {
        ApplicationListener<TestEvent> listener = event -> eventReceived = true;
        multicaster.addApplicationListener(listener);
        
        // Should not throw exception
        assertNotNull(multicaster);
    }

    @Test
    void testRemoveApplicationListener() {
        ApplicationListener<TestEvent> listener = event -> eventReceived = true;
        multicaster.addApplicationListener(listener);
        multicaster.removeApplicationListener(listener);
        
        // Should not throw exception
        assertNotNull(multicaster);
    }

    @Test
    void testMulticastEvent() {
        ApplicationListener<TestEvent> listener = event -> eventReceived = true;
        multicaster.addApplicationListener(listener);
        
        TestEvent event = new TestEvent(new Object());
        multicaster.multicastEvent(event);
        
        // Event should be received
        assertTrue(eventReceived);
    }

    @Test
    void testRemoveAllListeners() {
        ApplicationListener<TestEvent> listener = event -> eventReceived = true;
        multicaster.addApplicationListener(listener);
        multicaster.removeAllListeners();
        
        // Should not throw exception
        assertNotNull(multicaster);
    }

    static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }
}

