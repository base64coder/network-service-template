package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Bean;
import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Configuration;
import com.dtc.core.framework.ioc.annotation.Inject;
import com.dtc.core.framework.ioc.context.ApplicationContext;
import com.dtc.core.framework.ioc.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtcIocConfigurationTest {

    @Test
    public void testConfigurationAndBean() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.core.framework.ioc.test");
        MockDataSource dataSource = context.getBean(MockDataSource.class);
        assertNotNull(dataSource);
        assertEquals("jdbc:mysql://localhost:3306/test", dataSource.getUrl());
        
        MockJdbcTemplate jdbcTemplate = context.getBean(MockJdbcTemplate.class);
        assertNotNull(jdbcTemplate);
        assertSame(dataSource, jdbcTemplate.dataSource);
    }
}
