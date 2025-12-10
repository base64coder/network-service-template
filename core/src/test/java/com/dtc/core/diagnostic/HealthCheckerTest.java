package com.dtc.core.diagnostic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HealthChecker 测试
 */
@DisplayName("健康检查器测试")
public class HealthCheckerTest {

    private HealthChecker healthChecker;

    @BeforeEach
    void setUp() {
        healthChecker = new HealthChecker();
    }

    @Test
    @DisplayName("测试创建健康检查器")
    void testCreateHealthChecker() {
        assertNotNull(healthChecker);
    }

    @Test
    @DisplayName("测试执行健康检查")
    void testPerformHealthCheck() {
        assertDoesNotThrow(() -> {
            boolean healthy = healthChecker.isHealthy();
            // 默认应该是健康的
            assertTrue(healthy);
        });
    }

    @Test
    @DisplayName("测试获取健康状态")
    void testGetHealthStatus() {
        assertDoesNotThrow(() -> {
            String status = healthChecker.getHealthStatus();
            assertNotNull(status);
        });
    }
}

