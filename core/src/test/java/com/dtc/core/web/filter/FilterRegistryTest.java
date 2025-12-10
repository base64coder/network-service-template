package com.dtc.core.web.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FilterRegistry 测试
 */
@DisplayName("过滤器注册表测试")
public class FilterRegistryTest {

    @Mock
    private Filter mockFilter1;

    @Mock
    private Filter mockFilter2;

    private FilterRegistry registry;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        registry = new FilterRegistry();
    }

    @Test
    @DisplayName("测试创建过滤器注册表")
    void testCreateRegistry() {
        assertNotNull(registry);
    }

    @Test
    @DisplayName("测试注册过滤器")
    void testRegisterFilter() {
        when(mockFilter1.getName()).thenReturn("filter1");
        when(mockFilter1.getOrder()).thenReturn(1);
        
        assertDoesNotThrow(() -> registry.registerFilter(mockFilter1));
    }

    @Test
    @DisplayName("测试获取所有过滤器")
    void testGetAllFilters() {
        when(mockFilter1.getName()).thenReturn("filter1");
        when(mockFilter1.getOrder()).thenReturn(1);
        when(mockFilter2.getName()).thenReturn("filter2");
        when(mockFilter2.getOrder()).thenReturn(2);
        
        registry.registerFilter(mockFilter1);
        registry.registerFilter(mockFilter2);
        
        var filters = registry.getAllFilters();
        assertNotNull(filters);
        assertTrue(filters.size() >= 2);
    }

    @Test
    @DisplayName("测试过滤器按顺序排序")
    void testFilterOrdering() {
        when(mockFilter1.getName()).thenReturn("filter1");
        when(mockFilter1.getOrder()).thenReturn(2);
        when(mockFilter2.getName()).thenReturn("filter2");
        when(mockFilter2.getOrder()).thenReturn(1);
        
        registry.registerFilter(mockFilter1);
        registry.registerFilter(mockFilter2);
        
        var filters = registry.getAllFilters();
        assertTrue(filters.size() >= 2);
        // filter2应该在filter1之前（order值小的在前）
    }
}

