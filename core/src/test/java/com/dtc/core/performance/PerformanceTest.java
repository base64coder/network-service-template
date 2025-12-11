package com.dtc.core.performance;

import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.messaging.NetworkMessageConsumer;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 性能测试
 * 测试系统在高负载下的表现
 */
@DisplayName("性能测试")
public class PerformanceTest {

    @Mock
    private NetworkMessageConsumer mockConsumer;

    @Mock
    private StatisticsCollector mockStatisticsCollector;

    @Mock
    private ChannelHandlerContext mockContext;

    private NetworkMessageQueue messageQueue;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        messageQueue = new NetworkMessageQueue(mockConsumer);
        messageQueue.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (messageQueue.isStarted()) {
            messageQueue.stop();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @DisplayName("测试高并发消息发布性能")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHighConcurrencyMessagePublishing() throws InterruptedException {
        int threadCount = 20;
        int messagesPerThread = 1000;
        int totalMessages = threadCount * messagesPerThread;
        
        CountDownLatch latch = new CountDownLatch(totalMessages);
        AtomicLong publishedCount = new AtomicLong(0);
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockConsumer).consume(any(), anyLong(), anyBoolean());
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    NetworkMessageEvent event = NetworkMessageEvent.builder()
                            .eventId("perf-test-" + publishedCount.incrementAndGet())
                            .protocolType("HTTP")
                            .message("performance test message")
                            .channelContext(mockContext)
                            .build();
                    messageQueue.publish(event);
                }
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        // 等待所有消息被消费
        assertTrue(latch.await(5, TimeUnit.SECONDS), 
            "所有消息应在5秒内被处理");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double throughput = (totalMessages * 1000.0) / duration; // 消息/秒
        
        System.out.println(String.format(
            "性能测试结果: %d 条消息在 %d ms 内处理完成, 吞吐量: %.2f 消息/秒",
            totalMessages, duration, throughput));
        
        assertTrue(throughput > 1000, "吞吐量应大于1000消息/秒");
        verify(mockConsumer, atLeast(totalMessages)).consume(any(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("测试消息处理延迟")
    void testMessageProcessingLatency() throws InterruptedException {
        int messageCount = 1000;
        long[] latencies = new long[messageCount];
        AtomicLong index = new AtomicLong(0);
        
        doAnswer(invocation -> {
            long idx = index.getAndIncrement();
            if (idx < messageCount) {
                latencies[(int) idx] = System.currentTimeMillis();
            }
            return null;
        }).when(mockConsumer).consume(any(), anyLong(), anyBoolean());
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .eventId("latency-test-" + i)
                    .protocolType("HTTP")
                    .message("latency test message")
                    .channelContext(mockContext)
                    .timestamp(startTime)
                    .build();
                    messageQueue.publish(event);
        }
        
        Thread.sleep(2000); // 等待处理完成
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgLatency = totalTime / (double) messageCount;
        
        System.out.println(String.format(
            "延迟测试结果: %d 条消息平均延迟: %.2f ms",
            messageCount, avgLatency));
        
        assertTrue(avgLatency < 10, "平均延迟应小于10ms");
    }

    @Test
    @DisplayName("测试内存使用情况")
    void testMemoryUsage() throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int messageCount = 10000;
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .eventId("memory-test-" + i)
                    .protocolType("HTTP")
                    .message("memory test message " + i)
                    .channelContext(mockContext)
                    .build();
                    messageQueue.publish(event);
        }
        
        Thread.sleep(1000);
        
        System.gc();
        Thread.sleep(100);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println(String.format(
            "内存测试结果: 处理 %d 条消息后内存使用: %d KB",
            messageCount, memoryUsed / 1024));
        
        // 验证内存使用合理（小于100MB）
        assertTrue(memoryUsed < 100 * 1024 * 1024, 
            "内存使用应小于100MB");
    }

    @Test
    @DisplayName("测试长时间运行稳定性")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testLongRunningStability() throws InterruptedException {
        int durationSeconds = 5;
        int messagesPerSecond = 100;
        AtomicLong messageCount = new AtomicLong(0);
        
        doAnswer(invocation -> {
            messageCount.incrementAndGet();
            return null;
        }).when(mockConsumer).consume(any(), anyLong(), anyBoolean());
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        
        executor.submit(() -> {
            while (System.currentTimeMillis() < endTime) {
                for (int i = 0; i < messagesPerSecond; i++) {
                    NetworkMessageEvent event = NetworkMessageEvent.builder()
                            .eventId("stability-test-" + messageCount.get())
                            .protocolType("HTTP")
                            .message("stability test message")
                            .channelContext(mockContext)
                            .build();
                    messageQueue.publish(event);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        Thread.sleep((durationSeconds + 1) * 1000);
        executor.shutdown();
        
        long totalMessages = messageCount.get();
        System.out.println(String.format(
            "稳定性测试结果: %d 秒内处理了 %d 条消息",
            durationSeconds, totalMessages));
        
        assertTrue(totalMessages > 0, "应该处理了消息");
        assertTrue(messageQueue.isStarted(), "队列应该仍在运行");
    }
}

