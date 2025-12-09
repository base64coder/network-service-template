package com.dtc.framework.beans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationEventTest {

    @Test
    void testApplicationEvent() {
        Object source = new Object();
        ApplicationEvent event = new TestEvent(source);
        
        assertEquals(source, event.getSource());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testContextRefreshedEvent() {
        Object source = new Object();
        ContextRefreshedEvent event = new ContextRefreshedEvent(source);
        
        assertEquals(source, event.getSource());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testContextClosedEvent() {
        Object source = new Object();
        ContextClosedEvent event = new ContextClosedEvent(source);
        
        assertEquals(source, event.getSource());
        assertTrue(event.getTimestamp() > 0);
    }

    static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }
}

