package com.dtc.core.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MetricsRegistry 测试
 */
@DisplayName("指标注册表测试")
public class MetricsRegistryTest {

    private MetricsRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MetricsRegistry();
    }

    @Test
    @DisplayName("测试创建指标注册表")
    void testCreateRegistry() {
        assertNotNull(registry);
    }

    @Test
    @DisplayName("测试递增计数器")
    void testIncrementCounter() {
        registry.incrementCounter("test.counter", 1);
        registry.incrementCounter("test.counter", 5);
        
        long value = registry.getCounter("test.counter").get();
        assertEquals(6L, value);
    }

    @Test
    @DisplayName("测试递减计数器")
    void testDecrementCounter() {
        registry.incrementCounter("test.counter", 10);
        registry.incrementCounter("test.counter", -1);
        registry.incrementCounter("test.counter", -3);
        
        long value = registry.getCounter("test.counter").get();
        assertEquals(6L, value);
    }

    @Test
    @DisplayName("测试设置仪表盘")
    void testSetGauge() {
        registry.setGauge("test.gauge", 100L);
        assertEquals(100L, registry.getGauge("test.gauge"));
        
        registry.setGauge("test.gauge", 200L);
        assertEquals(200L, registry.getGauge("test.gauge"));
    }

    @Test
    @DisplayName("测试获取所有计数器")
    void testGetAllCounters() {
        registry.incrementCounter("counter1", 1);
        registry.incrementCounter("counter2", 5);
        
        Map<String, Long> counters = registry.getAllCounters();
        assertNotNull(counters);
        assertTrue(counters.size() >= 2);
        assertEquals(1L, counters.get("counter1"));
        assertEquals(5L, counters.get("counter2"));
    }

    @Test
    @DisplayName("测试获取所有仪表盘")
    void testGetAllGauges() {
        registry.setGauge("gauge1", 10L);
        registry.setGauge("gauge2", 20L);
        
        Map<String, Long> gauges = registry.getAllGauges();
        assertNotNull(gauges);
        assertTrue(gauges.size() >= 2);
        assertEquals(10L, gauges.get("gauge1"));
        assertEquals(20L, gauges.get("gauge2"));
    }

    @Test
    @DisplayName("测试重置所有指标")
    void testResetMetrics() {
        registry.incrementCounter("counter1", 1);
        registry.setGauge("gauge1", 10L);
        
        registry.reset();
        
        assertEquals(0L, registry.getCounter("counter1").get());
        assertEquals(0L, registry.getGauge("gauge1"));
    }

    @Test
    @DisplayName("测试获取不存在的计数器")
    void testGetNonExistentCounter() {
        AtomicLong counter = registry.getCounter("non.existent");
        assertNotNull(counter);
        assertEquals(0L, counter.get());
    }

    @Test
    @DisplayName("测试获取不存在的仪表盘")
    void testGetNonExistentGauge() {
        Long gauge = registry.getGauge("non.existent");
        // 可能返回null或默认值
    }

    @Test
    @DisplayName("测试并发递增计数器")
    void testConcurrentIncrementCounter() throws InterruptedException {
        int threadCount = 10;
        int incrementsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
        for (int j = 0; j < incrementsPerThread; j++) {
            registry.incrementCounter("concurrent.counter", 1);
        }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals((long) threadCount * incrementsPerThread, registry.getCounter("concurrent.counter").get());
    }
}

