package com.dtc.framework.test.module;

import com.dtc.framework.beans.module.AbstractModule;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.test.beans.module.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuiceCompatibilityTest {

    @Test
    public void testModuleBinding() {
        // 定义一个 Module
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                // 接口绑定到实现
                bind(ModuleService.class).to(ModuleServiceImpl.class).asEagerSingleton();
                
                // 实例绑定
                bind(ModuleConfig.class).toInstance(new ModuleConfig("Guice Works"));
            }
        };

        // 启动容器
        ApplicationContext context = new AnnotationConfigApplicationContext(module);

        // 验证 Service
        ModuleService service = context.getBean(ModuleService.class);
        assertNotNull(service);
        assertTrue(service instanceof ModuleServiceImpl);
        assertEquals("Hello Module", service.sayHello());

        // 验证 Config
        ModuleConfig config = context.getBean(ModuleConfig.class);
        assertNotNull(config);
        assertEquals("Guice Works", config.getValue());
    }
}

