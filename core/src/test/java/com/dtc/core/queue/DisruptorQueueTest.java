package com.dtc.core.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DisruptorQueue 测试
 */
@DisplayName("Disruptor队列测试")
public class DisruptorQueueTest {

    @Mock
    private QueueConsumer<TestEvent> mockConsumer;

    private DisruptorQueue<TestEvent> queue;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        queue = new DisruptorQueue<>(1024);
        queue.addConsumer(mockConsumer);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (queue != null) {
            queue.shutdown();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @DisplayName("测试队列创建")
    void testQueueCreation() {
        assertNotNull(queue);
    }

    @Test
    @DisplayName("测试队列启动")
    void testQueueStart() {
        assertDoesNotThrow(() -> queue.start());
    }

    @Test
    @DisplayName("测试队列停止")
    void testQueueStop() {
        queue.start();
        assertDoesNotThrow(() -> queue.shutdown());
    }

    @Test
    @DisplayName("测试发布事件")
    void testPublishEvent() {
        queue.start();
        
        TestEvent event = new TestEvent();
        event.setData("test data");
        
        assertTrue(queue.publish(event));
    }

    @Test
    @DisplayName("测试获取剩余容量")
    void testGetRemainingCapacity() {
        queue.start();
        long capacity = queue.getStatus().getRemainingCapacity();
        assertTrue(capacity > 0);
        assertTrue(capacity <= 1024);
    }

    // 测试事件类
    public static class TestEvent {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
