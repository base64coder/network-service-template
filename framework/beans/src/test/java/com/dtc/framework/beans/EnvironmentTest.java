package com.dtc.framework.beans;

import com.dtc.framework.beans.impl.DefaultEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    private DefaultEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = new DefaultEnvironment();
    }

    @Test
    void testGetProperty() {
        environment.setProperty("test.key", "test.value");
        
        String value = environment.getProperty("test.key");
        assertEquals("test.value", value);
    }

    @Test
    void testGetPropertyWithDefault() {
        String value = environment.getProperty("non.existent", "default");
        assertEquals("default", value);
    }

    @Test
    void testGetPropertyReturnsNull() {
        String value = environment.getProperty("non.existent");
        assertNull(value);
    }

    @Test
    void testContainsProperty() {
        environment.setProperty("test.key", "test.value");
        
        assertTrue(environment.containsProperty("test.key"));
        assertFalse(environment.containsProperty("non.existent"));
    }
}

