package com.dtc.framework.beans;

import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.Service;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeanScannerTest {

    @Test
    void testScanComponents() {
        List<Class<?>> components = BeanScanner.scanComponents("com.dtc.framework.beans");
        
        assertNotNull(components);
        // Should find at least some components if test classes exist
    }

    @Test
    void testScanEmptyPackage() {
        List<Class<?>> components = BeanScanner.scanComponents("com.nonexistent.package");
        
        assertNotNull(components);
        // Should return empty list for non-existent package
    }
}

class BeanScopeTest {

    @Test
    void testBeanScopeValues() {
        assertEquals("singleton", BeanScope.SINGLETON.name().toLowerCase());
        assertEquals("prototype", BeanScope.PROTOTYPE.name().toLowerCase());
    }

    @Test
    void testBeanScopeEnum() {
        BeanScope singleton = BeanScope.SINGLETON;
        BeanScope prototype = BeanScope.PROTOTYPE;
        
        assertNotNull(singleton);
        assertNotNull(prototype);
        assertNotEquals(singleton, prototype);
    }
}

