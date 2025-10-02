package com.dtc.core.serialization;

import com.dtc.api.annotations.NotNull;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Protobuf 性能测试 测试序列化/反序列化的性能表现
 * 
 * @author Network Service Template
 */
public class ProtobufPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ProtobufPerformanceTest.class);

    private OptimizedProtobufSerializer optimizedSerializer;
    private ProtobufSerializer originalSerializer;
    private BatchProtobufProcessor batchProcessor;
    private SerializationCacheManager cacheManager;

    @BeforeEach
    void setUp() {
        optimizedSerializer = new OptimizedProtobufSerializer();
        originalSerializer = new ProtobufSerializer();
        batchProcessor = new BatchProtobufProcessor();
        cacheManager = new SerializationCacheManager();
    }

    @Test
    @DisplayName("序列化性能对比测试")
    void testSerializationPerformance() {
        // 创建测试消息
        TestMessage message = createTestMessage();

        // 预热
        warmupSerializers(message);

        // 测试原始序列化器
        long originalTime = measureSerializationTime(originalSerializer, message, 10000);

        // 测试优化序列化器
        long optimizedTime = measureSerializationTime(optimizedSerializer, message, 10000);

        // 输出结果
        log.info("原始序列化器耗时: {}ms", originalTime);
        log.info("优化序列化器耗时: {}ms", optimizedTime);
        log.info("性能提升: {:.2f}%", (double) (originalTime - optimizedTime) / originalTime * 100);

        // 验证结果正确性
        byte[] originalData = originalSerializer.serialize(message);
        byte[] optimizedData = optimizedSerializer.serialize(message);
        assert java.util.Arrays.equals(originalData, optimizedData);
    }

    @Test
    @DisplayName("反序列化性能对比测试")
    void testDeserializationPerformance() {
        TestMessage message = createTestMessage();
        byte[] serializedData = originalSerializer.serialize(message);

        // 预热
        warmupDeserializers(serializedData);

        // 测试原始反序列化器
        long originalTime = measureDeserializationTime(originalSerializer, serializedData, 10000);

        // 测试优化反序列化器
        long optimizedTime = measureDeserializationTime(optimizedSerializer, serializedData, 10000);

        // 输出结果
        log.info("原始反序列化器耗时: {}ms", originalTime);
        log.info("优化反序列化器耗时: {}ms", optimizedTime);
        log.info("性能提升: {:.2f}%", (double) (originalTime - optimizedTime) / originalTime * 100);
    }

    @Test
    @DisplayName("批量处理性能测试")
    void testBatchProcessingPerformance() {
        List<TestMessage> messages = createTestMessages(1000);

        batchProcessor.start();

        try {
            // 测试批量添加
            long startTime = System.currentTimeMillis();
            int addedCount = batchProcessor.addMessages(messages);
            long addTime = System.currentTimeMillis() - startTime;

            log.info("批量添加 {} 条消息耗时: {}ms", addedCount, addTime);

            // 等待处理完成
            Thread.sleep(1000);

            // 获取统计信息
            BatchProtobufProcessor.QueueStatus status = batchProcessor.getQueueStatus();
            log.info("队列状态: {}", status);

        } finally {
            batchProcessor.stop();
        }
    }

    @Test
    @DisplayName("并发性能测试")
    void testConcurrentPerformance() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalTime = new AtomicLong(0);

        TestMessage message = createTestMessage();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();

                    for (int j = 0; j < messagesPerThread; j++) {
                        byte[] serialized = optimizedSerializer.serialize(message);
                        TestMessage deserialized = optimizedSerializer.deserialize(serialized, TestMessage.class);
                        assert deserialized != null;
                    }

                    long threadTime = System.currentTimeMillis() - startTime;
                    totalTime.addAndGet(threadTime);

                } catch (Exception e) {
                    log.error("Thread execution error", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long totalMessages = (long) threadCount * messagesPerThread;
        long avgTime = totalTime.get() / threadCount;

        log.info("并发测试完成: {} 线程, {} 消息/线程, 总消息数: {}, 平均耗时: {}ms", threadCount, messagesPerThread, totalMessages,
                avgTime);
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // 强制垃圾回收
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 创建大量消息
        List<TestMessage> messages = createTestMessages(10000);
        List<byte[]> serializedData = new ArrayList<>();

        for (TestMessage message : messages) {
            serializedData.add(optimizedSerializer.serialize(message));
        }

        long afterSerialization = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterSerialization - initialMemory;

        log.info("序列化 {} 条消息内存使用: {}MB", messages.size(), memoryUsed / 1024 / 1024);

        // 清理
        messages.clear();
        serializedData.clear();
        System.gc();

        long afterCleanup = runtime.totalMemory() - runtime.freeMemory();
        long memoryFreed = afterSerialization - afterCleanup;

        log.info("清理后释放内存: {}MB", memoryFreed / 1024 / 1024);
    }

    @Test
    @DisplayName("缓存效果测试")
    void testCacheEffectiveness() {
        TestMessage message = createTestMessage();

        // 预热缓存
        optimizedSerializer.warmupCache(TestMessage.class);

        // 测试缓存命中率
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            byte[] serialized = optimizedSerializer.serialize(message);
            TestMessage deserialized = optimizedSerializer.deserialize(serialized, TestMessage.class);
        }

        long endTime = System.currentTimeMillis();

        // 获取统计信息
        OptimizedProtobufSerializer.SerializationStats stats = optimizedSerializer.getStats();

        log.info("缓存统计: {}", stats);
        log.info("总耗时: {}ms", endTime - startTime);
        log.info("平均每次操作耗时: {}ns", stats.getAverageSerializeTime() + stats.getAverageDeserializeTime());
    }

    @Test
    @DisplayName("不同消息大小性能测试")
    void testDifferentMessageSizes() {
        int[] messageSizes = { 100, 1000, 10000, 100000 };

        for (int size : messageSizes) {
            TestMessage message = createTestMessageWithSize(size);

            long startTime = System.nanoTime();
            byte[] serialized = optimizedSerializer.serialize(message);
            TestMessage deserialized = optimizedSerializer.deserialize(serialized, TestMessage.class);
            long endTime = System.nanoTime();

            long totalTime = endTime - startTime;
            double throughput = (double) serialized.length / (totalTime / 1_000_000.0); // MB/s

            log.info("消息大小: {} bytes, 序列化大小: {} bytes, 耗时: {}ns, 吞吐量: {:.2f} MB/s", size, serialized.length, totalTime,
                    throughput);
        }
    }

    // 辅助方法

    private TestMessage createTestMessage() {
        return TestMessage.newBuilder().setId(1).setName("Test Message")
                .setDescription("This is a test message for performance testing")
                .setTimestamp(System.currentTimeMillis()).build();
    }

    private TestMessage createTestMessageWithSize(int targetSize) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < targetSize) {
            sb.append("This is a test message for performance testing. ");
        }

        return TestMessage.newBuilder().setId(1).setName("Test Message").setDescription(sb.toString())
                .setTimestamp(System.currentTimeMillis()).build();
    }

    private List<TestMessage> createTestMessages(int count) {
        List<TestMessage> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(TestMessage.newBuilder().setId(i).setName("Test Message " + i)
                    .setDescription("This is test message number " + i).setTimestamp(System.currentTimeMillis())
                    .build());
        }
        return messages;
    }

    private void warmupSerializers(TestMessage message) {
        for (int i = 0; i < 100; i++) {
            originalSerializer.serialize(message);
            optimizedSerializer.serialize(message);
        }
    }

    private void warmupDeserializers(byte[] data) {
        for (int i = 0; i < 100; i++) {
            originalSerializer.deserialize(data, TestMessage.class);
            optimizedSerializer.deserialize(data, TestMessage.class);
        }
    }

    private long measureSerializationTime(ProtobufSerializer serializer, TestMessage message, int iterations) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            serializer.serialize(message);
        }

        return System.currentTimeMillis() - startTime;
    }

    private long measureSerializationTime(OptimizedProtobufSerializer serializer, TestMessage message, int iterations) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            serializer.serialize(message);
        }

        return System.currentTimeMillis() - startTime;
    }

    private long measureDeserializationTime(ProtobufSerializer serializer, byte[] data, int iterations) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            serializer.deserialize(data, TestMessage.class);
        }

        return System.currentTimeMillis() - startTime;
    }

    private long measureDeserializationTime(OptimizedProtobufSerializer serializer, byte[] data, int iterations) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            serializer.deserialize(data, TestMessage.class);
        }

        return System.currentTimeMillis() - startTime;
    }

    // 测试用的 Protobuf 消息类
    public static class TestMessage extends com.google.protobuf.GeneratedMessageV3 {
        private final int id;
        private final String name;
        private final String description;
        private final long timestamp;

        private TestMessage(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.timestamp = builder.timestamp;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public Builder newBuilderForType() {
            return newBuilder();
        }

        @Override
        public Builder toBuilder() {
            return newBuilder().setId(id).setName(name).setDescription(description).setTimestamp(timestamp);
        }

        @Override
        public int getSerializedSize() {
            return 4 + name.length() + description.length() + 8; // 简化计算
        }

        @Override
        public byte[] toByteArray() {
            // 简化的序列化实现
            return (id + "|" + name + "|" + description + "|" + timestamp).getBytes();
        }

        public static class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> {
            private int id;
            private String name = "";
            private String description = "";
            private long timestamp;

            public Builder setId(int id) {
                this.id = id;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setDescription(String description) {
                this.description = description;
                return this;
            }

            public Builder setTimestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            @Override
            public TestMessage build() {
                return new TestMessage(this);
            }

            @Override
            public Builder clone() {
                return new Builder().setId(id).setName(name).setDescription(description).setTimestamp(timestamp);
            }

            @Override
            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof TestMessage) {
                    TestMessage otherMessage = (TestMessage) other;
                    return setId(otherMessage.id).setName(otherMessage.name).setDescription(otherMessage.description)
                            .setTimestamp(otherMessage.timestamp);
                }
                return this;
            }

            @Override
            public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
                // 简化的反序列化实现
                return this;
            }

            @Override
            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return null;
            }

            @Override
            public TestMessage getDefaultInstanceForType() {
                return new TestMessage(this);
            }
        }
    }
}
