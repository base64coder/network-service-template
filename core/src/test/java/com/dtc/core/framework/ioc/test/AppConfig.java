package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Bean;
import com.dtc.core.framework.ioc.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public MockDataSource dataSource() {
        return new MockDataSource("jdbc:mysql://localhost:3306/test");
    }
    
    @Bean
    public MockJdbcTemplate jdbcTemplate(MockDataSource dataSource) {
        return new MockJdbcTemplate(dataSource);
    }
}

