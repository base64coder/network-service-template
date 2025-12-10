package com.dtc.core.persistence.impl;

import com.dtc.core.persistence.DataSourceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JdbcRepository 测试
 */
@DisplayName("JDBC仓储测试")
public class JdbcRepositoryTest {

    @Mock
    private DataSourceProvider mockDataSourceProvider;

    @Mock
    private DataSource mockDataSource;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private TestJdbcRepository repository;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        
        when(mockDataSourceProvider.getDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        
        repository = new TestJdbcRepository(mockDataSourceProvider);
    }

    @Test
    @DisplayName("测试创建Repository")
    void testCreateRepository() {
        assertNotNull(repository);
    }

    // 测试用的JdbcRepository实现
    static class TestJdbcRepository extends JdbcRepository<TestEntity, Long> {
        public TestJdbcRepository(DataSourceProvider dataSourceProvider) {
            super(dataSourceProvider);
        }
    }

    // 测试实体类  
    @com.dtc.annotations.persistence.Table(value = "test_table")
    static class TestEntity {
        @com.dtc.annotations.persistence.Id
        private Long id;
        
        @com.dtc.annotations.persistence.Column(value = "name")
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

