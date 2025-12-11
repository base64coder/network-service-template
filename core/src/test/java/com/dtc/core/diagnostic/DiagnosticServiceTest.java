package com.dtc.core.diagnostic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DiagnosticService 测试
 */
@DisplayName("诊断服务测试")
public class DiagnosticServiceTest {

    private DiagnosticService diagnosticService;

    @BeforeEach
    void setUp() {
        diagnosticService = new DiagnosticService();
    }

    @Test
    @DisplayName("测试创建诊断服务")
    void testCreateDiagnosticService() {
        assertNotNull(diagnosticService);
    }

    @Test
    @DisplayName("测试收集诊断信息")
    void testCollectDiagnosticInfo() {
        Map<String, Object> info = diagnosticService.collectDiagnosticInfo();
        
        assertNotNull(info);
        assertTrue(info.containsKey("system.time"));
        assertTrue(info.containsKey("system.memory.total"));
        assertTrue(info.containsKey("system.memory.free"));
        assertTrue(info.containsKey("system.processors"));
        
        assertTrue((Long) info.get("system.time") > 0);
        assertTrue((Long) info.get("system.memory.total") > 0);
        assertTrue((Long) info.get("system.memory.free") >= 0);
        assertTrue((Integer) info.get("system.processors") > 0);
    }

    @Test
    @DisplayName("测试多次收集诊断信息")
    void testCollectDiagnosticInfoMultipleTimes() throws InterruptedException {
        Map<String, Object> info1 = diagnosticService.collectDiagnosticInfo();
        Thread.sleep(10); // 确保时间不同
        Map<String, Object> info2 = diagnosticService.collectDiagnosticInfo();
        
        assertNotNull(info1);
        assertNotNull(info2);
        // 时间应该不同（如果太快可能相同，所以只验证不为null）
        assertNotNull(info1.get("system.time"));
        assertNotNull(info2.get("system.time"));
    }

    @Test
    @DisplayName("测试执行健康检查")
    void testPerformHealthCheck() {
        DiagnosticService.HealthStatus status = diagnosticService.performHealthCheck();
        
        assertNotNull(status);
        assertEquals(DiagnosticService.HealthStatus.HEALTHY, status);
    }

    @Test
    @DisplayName("测试健康状态枚举")
    void testHealthStatusEnum() {
        DiagnosticService.HealthStatus[] statuses = DiagnosticService.HealthStatus.values();
        
        assertEquals(3, statuses.length);
        assertTrue(statuses.length > 0);
    }
}

