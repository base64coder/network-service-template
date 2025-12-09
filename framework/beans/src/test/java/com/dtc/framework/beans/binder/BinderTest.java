package com.dtc.framework.beans.binder;

import com.dtc.framework.beans.BeanScope;
import com.dtc.framework.beans.NetworkApplicationContext;
import com.dtc.framework.beans.impl.DefaultNetworkApplicationContext;
import com.dtc.framework.beans.impl.binder.DefaultBinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinderTest {

    private Binder binder;
    private NetworkApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new DefaultNetworkApplicationContext();
        binder = new DefaultBinder(context);
    }

    @Test
    void testBind() {
        LinkedBindingBuilder<String> builder = binder.bind(String.class);
        assertNotNull(builder);
    }

    @Test
    void testBindToInstance() {
        binder.bind(String.class).toInstance("testValue");
        
        String bean = context.getBean(String.class);
        assertEquals("testValue", bean);
    }

    @Test
    void testBindToImplementation() {
        LinkedBindingBuilder<TestInterface> builder = binder.bind(TestInterface.class);
        ScopedBindingBuilder scoped = builder.to(TestImplementation.class);
        
        assertNotNull(scoped);
    }

    @Test
    void testBindInScope() {
        LinkedBindingBuilder<String> builder = binder.bind(String.class);
        builder.in(BeanScope.PROTOTYPE);
        
        // Should not throw exception
        assertNotNull(builder);
    }

    @Test
    void testBindAsEagerSingleton() {
        LinkedBindingBuilder<String> builder = binder.bind(String.class);
        builder.asEagerSingleton();
        
        // Should not throw exception
        assertNotNull(builder);
    }

    interface TestInterface {
        void doSomething();
    }

    static class TestImplementation implements TestInterface {
        @Override
        public void doSomething() {}
    }
}

