package com.dtc.core.framework.ioc.test;

public class MockJdbcTemplate {
    public MockDataSource dataSource;
    public MockJdbcTemplate(MockDataSource dataSource) { this.dataSource = dataSource; }
}

