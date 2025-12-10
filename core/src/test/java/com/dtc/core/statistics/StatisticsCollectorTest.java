package com.dtc.core.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StatisticsCollector 测试
 */
@DisplayName("统计收集器测试")
public class StatisticsCollectorTest {

    private StatisticsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new StatisticsCollector();
    }

    @Test
    @DisplayName("测试请求开始")
    void testOnRequestStart() {
        assertDoesNotThrow(() -> collector.onRequestStart());
        assertTrue(collector.getTotalRequests() > 0);
    }

    @Test
    @DisplayName("测试请求完成")
    void testOnRequestComplete() {
        collector.onRequestStart();
        assertDoesNotThrow(() -> collector.onRequestComplete(100));
        assertTrue(collector.getTotalRequests() > 0);
    }

    @Test
    @DisplayName("测试请求错误")
    void testOnRequestError() {
        assertDoesNotThrow(() -> collector.onRequestError());
        assertTrue(collector.getErrorRequestCount() > 0);
    }

    @Test
    @DisplayName("测试获取统计信息")
    void testGetStatistics() {
        collector.onRequestStart();
        collector.onRequestComplete(100);
        collector.onRequestError();

        assertTrue(collector.getTotalRequests() > 0);
        assertTrue(collector.getErrorRequestCount() > 0);
        
        StatisticsCollector.StatisticsInfo info = collector.getStatistics();
        assertNotNull(info);
        assertTrue(info.getTotalRequests() > 0);
    }

    @Test
    @DisplayName("测试重置统计")
    void testResetStatistics() {
        collector.onRequestStart();
        collector.resetStatistics();
        assertEquals(0, collector.getTotalRequests());
        assertEquals(0, collector.getErrorRequestCount());
    }
    
    @Test
    @DisplayName("测试连接统计")
    void testConnectionStatistics() {
        collector.onConnectionEstablished();
        assertEquals(1, collector.getActiveConnections());
        assertEquals(1, collector.getTotalClients());
        
        collector.onConnectionClosed();
        assertEquals(0, collector.getActiveConnections());
    }
    
    @Test
    @DisplayName("测试平均处理时间")
    void testAverageProcessingTime() {
        collector.onRequestStart();
        collector.onRequestComplete(100);
        collector.onRequestStart();
        collector.onRequestComplete(200);
        
        double avgTime = collector.getAverageProcessingTime();
        assertEquals(150.0, avgTime, 0.1);
    }
}

