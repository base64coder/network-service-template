package com.dtc.core;

import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.messaging.MessageProcessor;
import com.dtc.core.messaging.NetworkMessageHandler;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.ByteString;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Protobuf 和 Disruptor 集成测试
 */
public class ProtobufDisruptorIntegrationTest {

    @Test
    public void testProtobufSerialization() {
        // 测试 Protobuf 序列化
        ProtobufSerializer serializer = new ProtobufSerializer();

        // 创建测试数据
        byte[] testData = "Hello Protobuf!".getBytes();
        ByteString byteString = ByteString.copyFrom(testData);

        // 序列化
        byte[] serialized = byteString.toByteArray();
        System.out.println("Serialized size: " + serialized.length + " bytes");

        // 反序列化
        ByteString deserialized = ByteString.copyFrom(serialized);
        System.out.println("Deserialized: " + deserialized.toStringUtf8());
    }

    @Test
    public void testDisruptorQueue() throws InterruptedException {
        // 测试 Disruptor 队列
        MessageProcessor processor = new MessageProcessor(new ProtobufSerializer());

        // 启动处理器
        processor.start();

        // 发送测试消息
        for (int i = 0; i < 10; i++) {
            byte[] testData = ("Test message " + i).getBytes();
            processor.processRawData(testData);
        }

        // 等待处理完成
        Thread.sleep(1000);

        // 获取统计信息
        MessageProcessor.ProcessingStats stats = processor.getStats();
        System.out.println("Processing stats: " + stats);

        // 关闭处理器
        processor.shutdown();
    }

    @Test
    public void testNetworkMessageHandler() throws InterruptedException {
        // 测试网络消息处理器
        ProtobufSerializer serializer = new ProtobufSerializer();
        MessageProcessor processor = new MessageProcessor(serializer);
        NetworkMessageHandler handler = new NetworkMessageHandler(serializer, processor);

        // 启动处理器
        processor.start();

        // 发送测试消息
        for (int i = 0; i < 5; i++) {
            byte[] testData = ("Network message " + i).getBytes();
            handler.handleRawData(testData);
        }

        // 等待处理完成
        Thread.sleep(1000);

        // 获取统计信息
        NetworkMessageHandler.HandlerStats stats = handler.getStats();
        System.out.println("Handler stats: " + stats);

        // 关闭处理器
        processor.shutdown();
    }

    @Test
    public void testFullIntegration() throws InterruptedException {
        // 测试完整集成
        ServerConfiguration config = ServerConfiguration.builder().serverName("Integration Test Server")
                .serverVersion("1.0.0").build();

        NetworkService service = new NetworkService(config);

        // 启动服务
        service.start().get(5, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler handler = service.getMessageHandler();

        // 发送测试消息
        for (int i = 0; i < 10; i++) {
            byte[] testData = ("Integration test message " + i).getBytes();
            handler.handleRawData(testData);
        }

        // 等待处理完成
        Thread.sleep(2000);

        // 获取统计信息
        NetworkMessageHandler.HandlerStats stats = service.getMessageStats();
        System.out.println("Full integration stats: " + stats);

        // 停止服务
        service.stop().get(5, TimeUnit.SECONDS);
    }
}
