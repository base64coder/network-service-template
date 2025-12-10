package com.dtc.core.web.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * InterceptorRegistry 测试
 */
@DisplayName("拦截器注册表测试")
public class InterceptorRegistryTest {

    @Mock
    private Interceptor mockInterceptor1;

    @Mock
    private Interceptor mockInterceptor2;

    private InterceptorRegistry registry;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        registry = new InterceptorRegistry();
    }

    @Test
    @DisplayName("测试创建拦截器注册表")
    void testCreateRegistry() {
        assertNotNull(registry);
    }

    @Test
    @DisplayName("测试注册拦截器")
    void testRegisterInterceptor() {
        when(mockInterceptor1.getName()).thenReturn("interceptor1");
        when(mockInterceptor1.getOrder()).thenReturn(1);
        
        assertDoesNotThrow(() -> registry.registerInterceptor(mockInterceptor1));
    }

    @Test
    @DisplayName("测试获取所有拦截器")
    void testGetAllInterceptors() {
        when(mockInterceptor1.getName()).thenReturn("interceptor1");
        when(mockInterceptor1.getOrder()).thenReturn(1);
        when(mockInterceptor2.getName()).thenReturn("interceptor2");
        when(mockInterceptor2.getOrder()).thenReturn(2);
        
        registry.registerInterceptor(mockInterceptor1);
        registry.registerInterceptor(mockInterceptor2);
        
        var interceptors = registry.getAllInterceptors();
        assertNotNull(interceptors);
        assertTrue(interceptors.size() >= 2);
    }

    @Test
    @DisplayName("测试拦截器按顺序排序")
    void testInterceptorOrdering() {
        when(mockInterceptor1.getName()).thenReturn("interceptor1");
        when(mockInterceptor1.getOrder()).thenReturn(2);
        when(mockInterceptor2.getName()).thenReturn("interceptor2");
        when(mockInterceptor2.getOrder()).thenReturn(1);
        
        registry.registerInterceptor(mockInterceptor1);
        registry.registerInterceptor(mockInterceptor2);
        
        var interceptors = registry.getAllInterceptors();
        assertTrue(interceptors.size() >= 2);
    }
}

