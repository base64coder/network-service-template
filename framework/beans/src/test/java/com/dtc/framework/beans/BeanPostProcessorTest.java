package com.dtc.framework.beans;

import com.dtc.framework.beans.impl.DefaultBeanDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanPostProcessorTest {

    @Test
    void testBeanPostProcessor() {
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

        String bean = "test";
        Object result1 = processor.postProcessBeforeInitialization(bean, "testBean");
        Object result2 = processor.postProcessAfterInitialization(bean, "testBean");

        assertEquals(bean, result1);
        assertEquals(bean, result2);
    }
}

