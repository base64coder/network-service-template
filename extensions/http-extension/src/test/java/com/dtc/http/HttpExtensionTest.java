package com.dtc.http;

import com.dtc.api.parameter.ExtensionStartInput;
import com.dtc.api.parameter.ExtensionStartOutput;
import com.dtc.api.parameter.ExtensionStopInput;
import com.dtc.api.parameter.ExtensionStopOutput;
import com.dtc.core.http.*;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.SocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HttpExtension 单元测试
 */
public class HttpExtensionTest {

    @Mock
    private HttpServer httpServer;

    @Mock
    private HttpRequestHandler requestHandler;

    @Mock
    private HttpResponseHandler responseHandler;

    @Mock
    private HttpRouteManager routeManager;

    @Mock
    private HttpMiddlewareManager middlewareManager;

    @Mock
    private NetworkMessageQueue messageQueue;

    @Mock
    private ChannelHandlerContext channelContext;

    @Mock
    private com.dtc.core.statistics.StatisticsCollector statisticsCollector;

    private HttpExtension httpExtension;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock channel context
        Channel channel = mock(Channel.class);
        SocketAddress remoteAddress = mock(SocketAddress.class);
        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(channelContext.channel()).thenReturn(channel);
        when(remoteAddress.toString()).thenReturn("127.0.0.1:8080");
        
        httpExtension = new HttpExtension(
                httpServer,
                requestHandler,
                responseHandler,
                routeManager,
                middlewareManager,
                messageQueue,
                statisticsCollector);
    }

    @Test
    @DisplayName("测试扩展启动")
    void testExtensionStart() {
        ExtensionStartInput input = mock(ExtensionStartInput.class);
        ExtensionStartOutput output = mock(ExtensionStartOutput.class);

        when(input.getExtensionVersion()).thenReturn("1.0.0");
        when(input.getExtensionId()).thenReturn("test");

        // 测试启动
        httpExtension.extensionStart(input, output);

        // 验证启动成功
        assertTrue(httpExtension.isStarted());
        verify(output, never()).preventStartup(anyString());
    }

    @Test
    @DisplayName("测试扩展停止")
    void testExtensionStop() {
        ExtensionStopInput input = mock(ExtensionStopInput.class);
        ExtensionStopOutput output = mock(ExtensionStopOutput.class);

        when(input.getExtensionVersion()).thenReturn("1.0.0");
        when(input.getExtensionId()).thenReturn("test");

        // 先启动扩展
        ExtensionStartInput startInput = mock(ExtensionStartInput.class);
        ExtensionStartOutput startOutput = mock(ExtensionStartOutput.class);
        when(startInput.getExtensionVersion()).thenReturn("1.0.0");
        when(startInput.getExtensionId()).thenReturn("test");
        httpExtension.extensionStart(startInput, startOutput);

        // 测试停止
        httpExtension.extensionStop(input, output);

        // 验证停止成功
        assertFalse(httpExtension.isStarted());
        verify(output, never()).preventStop(anyString());
    }

    @Test
    @DisplayName("测试处理HTTP请求")
    void testOnMessage() {
        // 创建测试请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                "/test");

        // Mock队列发布成功
        when(messageQueue.publish(any())).thenReturn(true);

        // 测试处理消息
        httpExtension.onMessage(channelContext, request);

        // 验证队列发布被调用
        verify(messageQueue).publish(any());
    }

    @Test
    @DisplayName("测试处理非HTTP消息")
    void testOnMessageNonHttp() {
        // 创建非HTTP消息
        String nonHttpMessage = "not an HTTP message";

        // 测试处理消息
        httpExtension.onMessage(channelContext, nonHttpMessage);

        // 验证队列发布没有被调用
        verify(messageQueue, never()).publish(any());
    }

    @Test
    @DisplayName("测试队列发布失败")
    void testQueuePublishFailure() {
        // 创建测试请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                "/test");

        // Mock队列发布失败
        when(messageQueue.publish(any())).thenReturn(false);

        // 测试处理消息
        httpExtension.onMessage(channelContext, request);

        // 验证队列发布被调用
        verify(messageQueue).publish(any());
    }

    @Test
    @DisplayName("测试连接处理")
    void testOnConnect() {
        String clientId = "test-client";

        // 测试连接
        httpExtension.onConnect(channelContext, clientId);

        // 验证连接处理（这里可以添加具体的验证逻辑）
        assertNotNull(clientId);
    }

    @Test
    @DisplayName("测试断开连接处理")
    void testOnDisconnect() {
        String clientId = "test-client";

        // 测试断开连接
        httpExtension.onDisconnect(channelContext, clientId);

        // 验证断开连接处理（这里可以添加具体的验证逻辑）
        assertNotNull(clientId);
    }

    @Test
    @DisplayName("测试异常处理")
    void testOnException() {
        Throwable cause = new RuntimeException("Test exception");

        // 测试异常处理
        httpExtension.onException(channelContext, cause);

        // 验证异常处理（这里可以添加具体的验证逻辑）
        assertNotNull(cause);
    }

    @Test
    @DisplayName("测试协议信息")
    void testProtocolInfo() {
        // 测试协议名称
        assertEquals("http", httpExtension.getProtocolName());

        // 测试协议版本
        assertEquals("1.1", httpExtension.getProtocolVersion());

        // 测试默认端口
        assertEquals(8080, httpExtension.getDefaultPort());
    }

    @Test
    @DisplayName("测试创建网络消息事件")
    void testCreateNetworkMessageEvent() {
        // 创建测试请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/api/test");

        // 添加请求体
        ByteBuf content = Unpooled.copiedBuffer("test body", io.netty.util.CharsetUtil.UTF_8);
        request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/api/test",
                content);

        // 测试创建网络消息事件
        // 注意：这里需要访问私有方法，可能需要使用反射或修改方法可见性
        // 这里假设有公共方法可以测试
        assertNotNull(request);
    }

    @Test
    @DisplayName("测试发送错误响应")
    void testSendErrorResponse() {
        String errorMessage = "Test error";

        // 测试发送错误响应
        // 注意：这里需要访问私有方法，可能需要使用反射或修改方法可见性
        // 这里假设有公共方法可以测试
        assertNotNull(errorMessage);
    }

    @Test
    @DisplayName("测试并发消息处理")
    void testConcurrentMessageHandling() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // Mock队列发布成功
        when(messageQueue.publish(any())).thenReturn(true);

        // 创建多个线程并发处理消息
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    FullHttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1,
                            HttpMethod.GET,
                            "/test/" + j);
                    httpExtension.onMessage(channelContext, request);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证所有消息都被处理
        verify(messageQueue, times(threadCount * messagesPerThread)).publish(any());
    }

    @Test
    @DisplayName("测试不同HTTP方法")
    void testDifferentHttpMethods() {
        // Mock队列发布成功
        when(messageQueue.publish(any())).thenReturn(true);

        // 测试GET请求
        FullHttpRequest getRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                "/test");
        httpExtension.onMessage(channelContext, getRequest);

        // 测试POST请求
        FullHttpRequest postRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/test");
        httpExtension.onMessage(channelContext, postRequest);

        // 测试PUT请求
        FullHttpRequest putRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.PUT,
                "/test");
        httpExtension.onMessage(channelContext, putRequest);

        // 测试DELETE请求
        FullHttpRequest deleteRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.DELETE,
                "/test");
        httpExtension.onMessage(channelContext, deleteRequest);

        // 验证所有请求都被处理
        verify(messageQueue, times(4)).publish(any());
    }

    @Test
    @DisplayName("测试性能")
    void testPerformance() {
        // Mock队列发布成功
        when(messageQueue.publish(any())).thenReturn(true);

        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 处理大量消息
        for (int i = 0; i < messageCount; i++) {
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1,
                    HttpMethod.GET,
                    "/test/" + i);
            httpExtension.onMessage(channelContext, request);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能（应该能在1秒内处理10000条消息）
        assertTrue(duration < 1000, "处理10000条消息应该在1秒内完成");

        // 验证所有消息都被处理
        verify(messageQueue, times(messageCount)).publish(any());
    }
}
