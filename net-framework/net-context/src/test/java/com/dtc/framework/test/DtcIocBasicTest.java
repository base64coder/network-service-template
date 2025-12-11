package com.dtc.framework.test;

import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.test.beans.basic.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtcIocBasicTest {

    @Test
    public void testSimpleInjection() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.basic");
        UserService userService = context.getBean(UserService.class);
        assertNotNull(userService);
        assertNotNull(userService.userRepository);
    }

    @Test
    public void testSingletonScope() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.basic");
        UserService s1 = context.getBean(UserService.class);
        UserService s2 = context.getBean(UserService.class);
        assertSame(s1, s2);
    }

    @Test
    public void testPrototypeScope() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.basic");
        PrototypeBean p1 = context.getBean(PrototypeBean.class);
        PrototypeBean p2 = context.getBean(PrototypeBean.class);
        assertNotSame(p1, p2);
    }

    @Test
    public void testLifecycleAnnotations() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.basic");
        LifecycleBean bean = context.getBean(LifecycleBean.class);
        assertTrue(bean.isInitialized());
        assertFalse(bean.isDestroyed());
        
        context.close();
        assertTrue(bean.isDestroyed());
    }
}

