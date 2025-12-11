package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Bean;
import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.ConditionalOnClass;
import com.dtc.core.framework.ioc.annotation.ConditionalOnMissingBean;
import com.dtc.core.framework.ioc.annotation.Configuration;
import com.dtc.core.framework.ioc.context.ApplicationContext;
import com.dtc.core.framework.ioc.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtcIocConditionalTest {

    @Test
    public void testConditionalOnClass() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        // 如果java.lang.String存在（肯定存在），ConditionalBean应该被注册
        assertTrue(context.containsBean("conditionalBean"));
    }

    @Test
    public void testConditionalOnMissingBean() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        // PrimaryBean应该被注册
        assertTrue(context.containsBean("primaryBean"));
        // ConditionalOnMissingBeanBean不应该被注册（因为PrimaryBean已存在）
        assertFalse(context.containsBean("conditionalOnMissingBeanBean"));
    }
}

