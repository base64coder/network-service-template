package com.dtc.core.messaging.handler;

import com.dtc.core.http.HttpRequestEx;
import com.dtc.core.http.HttpRequestHandler;
import com.dtc.core.http.HttpResponseEx;
import com.dtc.core.messaging.NetworkMessageEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HttpMessageHandler 单元测试
 */
public class HttpMessageHandlerTest {

        @Mock
        private HttpRequestHandler requestHandler;

        @Mock
        private ChannelHandlerContext channelContext;

        @Mock
        private Channel channel;

        private HttpMessageHandler httpMessageHandler;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                // 设置channelContext.channel()返回mock channel
                when(channelContext.channel()).thenReturn(channel);
                // 设置channel.isActive()返回true
                when(channel.isActive()).thenReturn(true);
                // 设置channel的writeAndFlush方法返回ChannelFuture
                when(channel.writeAndFlush(any())).thenReturn(mock(io.netty.channel.ChannelFuture.class));
                // 设置ctx.writeAndFlush返回ChannelFuture
                when(channelContext.writeAndFlush(any())).thenReturn(mock(io.netty.channel.ChannelFuture.class));
                // 默认Mock requestHandler返回一个非null响应
                HttpResponseEx defaultResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(defaultResponse);
                httpMessageHandler = new HttpMessageHandler(requestHandler);
        }

        @Test
        @DisplayName("测试处理HTTP消息")
        void testHandleMessage() {
                // 创建测试请求
                FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");

                // 确保content不为null
                assertNotNull(nettyRequest.content(), "Request content should not be null");

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nettyRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 测试处理消息
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("处理消息时抛出异常: " + e.getMessage());
                        e.printStackTrace();
                        fail("处理消息时不应该抛出异常: " + e.getMessage());
                }

                // 验证requestHandler被调用
                verify(requestHandler, atLeastOnce()).handleRequest(any(HttpRequestEx.class));

                // 验证ctx.writeAndFlush被调用
                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("测试处理POST请求")
        void testHandlePostRequest() {
                // 创建POST请求
                ByteBuf content = Unpooled.copiedBuffer("test body", io.netty.util.CharsetUtil.UTF_8);
                FullHttpRequest postRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.POST,
                                "/api/test",
                                content);

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(postRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 测试处理消息
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("处理消息时抛出异常: " + e.getMessage());
                        e.printStackTrace();
                        fail("处理消息时不应该抛出异常: " + e.getMessage());
                }

                // 验证请求处理器被调用
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 验证ctx.writeAndFlush被调用
                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("测试处理非FullHttpRequest消息")
        void testHandleNonFullHttpRequestMessage() {
                // 创建非FullHttpRequest消息
                String nonHttpMessage = "not an HTTP request";

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nonHttpMessage)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // 测试处理消息
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("处理消息时抛出异常: " + e.getMessage());
                        e.printStackTrace();
                        fail("处理消息时不应该抛出异常: " + e.getMessage());
                }

                // 验证请求处理器没有被调用
                verify(requestHandler, never()).handleRequest(any());

                // 验证响应没有被发送
                verify(channelContext, never()).writeAndFlush(any());
        }

        @Test
        @DisplayName("测试处理空消息")
        void testHandleNullMessage() {
                // 创建空消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(null)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // 测试处理消息（不应该抛出异常）
                assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));

                // 验证请求处理器没有被调用
                verify(requestHandler, never()).handleRequest(any());
        }

        @Test
        @DisplayName("测试请求处理器异常")
        void testRequestHandlerException() {
                // 创建测试请求
                FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nettyRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock请求处理器抛出异常
                when(requestHandler.handleRequest(any(HttpRequestEx.class)))
                                .thenThrow(new RuntimeException("Request handler error"));

                // 测试处理消息（不应该抛出异常）
                assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));

                // 验证请求处理器被调用
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 验证channel.isActive()被调用（handleError中调用）
                verify(channel, atLeastOnce()).isActive();
        }

        @Test
        @DisplayName("测试不同HTTP方法")
        void testDifferentHttpMethods() {
                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 测试GET请求
                FullHttpRequest getRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");
                NetworkMessageEvent getEvent = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(getRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();
                httpMessageHandler.handleMessage(getEvent);

                // 测试POST请求
                FullHttpRequest postRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.POST,
                                "/test");
                NetworkMessageEvent postEvent = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(postRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();
                httpMessageHandler.handleMessage(postEvent);

                // 测试PUT请求
                FullHttpRequest putRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.PUT,
                                "/test");
                NetworkMessageEvent putEvent = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(putRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();
                httpMessageHandler.handleMessage(putEvent);

                // 测试DELETE请求
                FullHttpRequest deleteRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.DELETE,
                                "/test");
                NetworkMessageEvent deleteEvent = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(deleteRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();
                httpMessageHandler.handleMessage(deleteEvent);

                // 验证所有请求都被处理
                verify(requestHandler, times(4)).handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(4)).writeAndFlush(mockResponse);
        }

        @Test
        @DisplayName("测试带请求头的HTTP请求")
        void testHttpRequestWithHeaders() {
                // 创建带请求头的HTTP请求
                FullHttpRequest request = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");
                request.headers().set("Content-Type", "application/json");
                request.headers().set("Authorization", "Bearer token");
                request.headers().set("User-Agent", "Test Client");

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(request)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 测试处理消息
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("处理消息时抛出异常: " + e.getMessage());
                        e.printStackTrace();
                        fail("处理消息时不应该抛出异常: " + e.getMessage());
                }

                // 验证请求处理器被调用
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 验证ctx.writeAndFlush被调用
                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("测试带请求体的HTTP请求")
        void testHttpRequestWithBody() {
                // 创建带请求体的POST请求
                ByteBuf content = Unpooled.copiedBuffer("{\"key\": \"value\"}", io.netty.util.CharsetUtil.UTF_8);
                FullHttpRequest request = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.POST,
                                "/api/test",
                                content);
                request.headers().set("Content-Type", "application/json");
                request.headers().set("Content-Length", content.readableBytes());

                // 确保content不为null且可读
                assertNotNull(request.content(), "Request content should not be null");
                assertTrue(request.content().readableBytes() > 0, "Request content should have readable bytes");

                // 创建网络消息事件
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(request)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 测试处理消息
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("处理消息时抛出异常: " + e.getMessage());
                        e.printStackTrace();
                        fail("处理消息时不应该抛出异常: " + e.getMessage());
                }

                // 验证请求处理器被调用
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 验证ctx.writeAndFlush被调用
                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("测试并发处理")
        void testConcurrentHandling() throws InterruptedException {
                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                int threadCount = 10;
                int requestsPerThread = 100;
                Thread[] threads = new Thread[threadCount];

                // 创建多个线程并发处理请求
                for (int i = 0; i < threadCount; i++) {
                        threads[i] = new Thread(() -> {
                                for (int j = 0; j < requestsPerThread; j++) {
                                        FullHttpRequest request = new DefaultFullHttpRequest(
                                                        HttpVersion.HTTP_1_1,
                                                        HttpMethod.GET,
                                                        "/test/" + j);
                                        NetworkMessageEvent event = NetworkMessageEvent.builder()
                                                        .protocolType("http")
                                                        .message(request)
                                                        .channelContext(channelContext)
                                                        .messageType("HTTP_REQUEST")
                                                        .build();

                                        httpMessageHandler.handleMessage(event);
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

                // 验证所有请求都被处理
                verify(requestHandler, times(threadCount * requestsPerThread))
                                .handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(threadCount * requestsPerThread))
                                .writeAndFlush(mockResponse);
        }

        @Test
        @DisplayName("测试性能")
        void testPerformance() {
                // Mock请求处理器返回响应
                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                int requestCount = 10000;
                long startTime = System.currentTimeMillis();

                // 处理大量请求
                for (int i = 0; i < requestCount; i++) {
                        FullHttpRequest request = new DefaultFullHttpRequest(
                                        HttpVersion.HTTP_1_1,
                                        HttpMethod.GET,
                                        "/test/" + i);
                        NetworkMessageEvent event = NetworkMessageEvent.builder()
                                        .protocolType("http")
                                        .message(request)
                                        .channelContext(channelContext)
                                        .messageType("HTTP_REQUEST")
                                        .build();

                        httpMessageHandler.handleMessage(event);
                }

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // 验证性能（应该能在合理时间内处理10000个请求）
                assertTrue(duration < 5000, "处理10000个请求应该在5秒内完成");

                // 验证所有请求都被处理
                verify(requestHandler, times(requestCount)).handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(requestCount)).writeAndFlush(mockResponse);
        }
}
