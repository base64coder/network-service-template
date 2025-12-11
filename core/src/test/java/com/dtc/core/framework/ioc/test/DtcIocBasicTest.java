package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;
import com.dtc.core.framework.ioc.annotation.PostConstruct;
import com.dtc.core.framework.ioc.annotation.PreDestroy;
import com.dtc.core.framework.ioc.annotation.Scope;
import com.dtc.core.framework.ioc.context.ApplicationContext;
import com.dtc.core.framework.ioc.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtcIocBasicTest {

    @Test
    public void testSimpleInjection() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        UserService userService = context.getBean(UserService.class);
        assertNotNull(userService);
        assertNotNull(userService.userRepository);
    }

    @Test
    public void testSingletonScope() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        UserService s1 = context.getBean(UserService.class);
        UserService s2 = context.getBean(UserService.class);
        assertSame(s1, s2);
    }

    @Test
    public void testPrototypeScope() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        PrototypeBean p1 = context.getBean(PrototypeBean.class);
        PrototypeBean p2 = context.getBean(PrototypeBean.class);
        assertNotSame(p1, p2);
    }

    @Test
    public void testLifecycleAnnotations() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        LifecycleBean bean = context.getBean(LifecycleBean.class);
        assertTrue(bean.isInitialized());
        assertFalse(bean.isDestroyed());
        
        context.close();
        assertTrue(bean.isDestroyed());
    }
}

