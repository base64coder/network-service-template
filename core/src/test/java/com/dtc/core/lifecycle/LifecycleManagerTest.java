package com.dtc.core.lifecycle;

import com.dtc.api.Lifecycle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LifecycleManager 测试
 */
@DisplayName("生命周期管理器测试")
public class LifecycleManagerTest {

    @Mock
    private Lifecycle mockLifecycle1;

    @Mock
    private Lifecycle mockLifecycle2;

    private LifecycleManager manager;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        manager = new LifecycleManager();
    }

    @Test
    @DisplayName("测试创建LifecycleManager")
    void testCreateManager() {
        assertNotNull(manager);
    }

    @Test
    @DisplayName("测试注册生命周期组件")
    void testRegisterLifecycle() {
        assertDoesNotThrow(() -> manager.registerLifecycle(mockLifecycle1));
    }

    @Test
    @DisplayName("测试启动所有组件")
    void testStartAll() throws Exception {
        manager.registerLifecycle(mockLifecycle1);
        manager.registerLifecycle(mockLifecycle2);
        
        manager.startAll();
        
        verify(mockLifecycle1, times(1)).start();
        verify(mockLifecycle2, times(1)).start();
    }

    @Test
    @DisplayName("测试停止所有组件")
    void testStopAll() throws Exception {
        manager.registerLifecycle(mockLifecycle1);
        manager.registerLifecycle(mockLifecycle2);
        
        manager.startAll();
        manager.stopAll();
        
        verify(mockLifecycle1, times(1)).stop();
        verify(mockLifecycle2, times(1)).stop();
    }

    @Test
    @DisplayName("测试组件启动失败")
    void testStartFailure() throws Exception {
        doThrow(new RuntimeException("启动失败")).when(mockLifecycle1).start();
        
        manager.registerLifecycle(mockLifecycle1);
        
        assertThrows(Exception.class, () -> manager.startAll());
    }
}

