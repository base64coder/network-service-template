package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Bean;
import com.dtc.core.framework.ioc.annotation.Configuration;

class DataSource {
    private String url;
    public DataSource(String url) { this.url = url; }
    public String getUrl() { return url; }
}

class JdbcTemplate {
    DataSource dataSource;
    public JdbcTemplate(DataSource dataSource) { this.dataSource = dataSource; }
}

@Configuration
class AppConfig {
    
    @Bean
    public DataSource dataSource() {
        return new DataSource("jdbc:mysql://localhost:3306/test");
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

