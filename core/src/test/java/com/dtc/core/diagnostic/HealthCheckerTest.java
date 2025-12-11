package com.dtc.core.diagnostic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HealthChecker 测试
 */
@DisplayName("健康检查器测试")
public class HealthCheckerTest {

    @Mock
    private DiagnosticService mockDiagnosticService;

    private HealthChecker healthChecker;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        healthChecker = new HealthChecker(mockDiagnosticService);
    }

    @Test
    @DisplayName("测试创建健康检查器")
    void testCreateHealthChecker() {
        assertNotNull(healthChecker);
    }

    @Test
    @DisplayName("测试启动健康检查器")
    void testStart() {
        assertDoesNotThrow(() -> healthChecker.start());
    }

    @Test
    @DisplayName("测试停止健康检查器")
    void testStop() {
        healthChecker.start();
        assertDoesNotThrow(() -> healthChecker.stop());
    }
}

