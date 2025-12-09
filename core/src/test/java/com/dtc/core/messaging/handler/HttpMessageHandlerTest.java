package com.dtc.core.messaging.handler;

import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpRequestHandler;
import com.dtc.core.network.http.HttpResponseEx;
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
 * HttpMessageHandler 氓聧聲氓聟聝忙碌聥猫炉聲
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
                // 猫庐戮莽陆庐channelContext.channel()猫驴聰氓聸聻mock channel
                when(channelContext.channel()).thenReturn(channel);
                // 猫庐戮莽陆庐channel.isActive()猫驴聰氓聸聻true
                when(channel.isActive()).thenReturn(true);
                // 猫庐戮莽陆庐channel莽職聞writeAndFlush忙聳鹿忙鲁聲猫驴聰氓聸聻ChannelFuture
                when(channel.writeAndFlush(any())).thenReturn(mock(io.netty.channel.ChannelFuture.class));
                // 猫庐戮莽陆庐ctx.writeAndFlush猫驴聰氓聸聻ChannelFuture
                when(channelContext.writeAndFlush(any())).thenReturn(mock(io.netty.channel.ChannelFuture.class));
                // 茅禄聵猫庐陇Mock requestHandler猫驴聰氓聸聻盲赂聙盲赂陋茅聺聻null氓聯聧氓潞聰
                HttpResponseEx defaultResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(defaultResponse);
                httpMessageHandler = new HttpMessageHandler(requestHandler);
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠HTTP忙露聢忙聛炉")
        void testHandleMessage() {
                // 氓聢聸氓禄潞忙碌聥猫炉聲猫炉路忙卤聜
                FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");

                // 莽隆庐盲驴聺content盲赂聧盲赂潞null
                assertNotNull(nettyRequest.content(), "Request content should not be null");

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nettyRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("氓陇聞莽聬聠忙露聢忙聛炉忙聴露忙聤聸氓聡潞氓录聜氓赂? " + e.getMessage());
                        e.printStackTrace();
                        fail("氓陇聞莽聬聠忙露聢忙聛炉忙聴露盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂: " + e.getMessage());
                }

                // 茅陋聦猫炉聛requestHandler猫垄芦猫掳聝莽聰?                verify(requestHandler, atLeastOnce()).handleRequest(any(HttpRequestEx.class));

                // 茅陋聦猫炉聛ctx.writeAndFlush猫垄芦猫掳聝莽聰?                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠POST猫炉路忙卤聜")
        void testHandlePostRequest() {
                // 氓聢聸氓禄潞POST猫炉路忙卤聜
                ByteBuf content = Unpooled.copiedBuffer("test body", io.netty.util.CharsetUtil.UTF_8);
                FullHttpRequest postRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.POST,
                                "/api/test",
                                content);

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(postRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("氓陇聞莽聬聠忙露聢忙聛炉忙聴露忙聤聸氓聡潞氓录聜氓赂? " + e.getMessage());
                        e.printStackTrace();
                        fail("氓陇聞莽聬聠忙露聢忙聛炉忙聴露盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂: " + e.getMessage());
                }

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 茅陋聦猫炉聛ctx.writeAndFlush猫垄芦猫掳聝莽聰?                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠茅聺聻FullHttpRequest忙露聢忙聛炉")
        void testHandleNonFullHttpRequestMessage() {
                // 氓聢聸氓禄潞茅聺聻FullHttpRequest忙露聢忙聛炉
                String nonHttpMessage = "not an HTTP request";

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nonHttpMessage)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("氓陇聞莽聬聠忙露聢忙聛炉忙聴露忙聤聸氓聡潞氓录聜氓赂? " + e.getMessage());
                        e.printStackTrace();
                        fail("氓陇聞莽聬聠忙露聢忙聛炉忙聴露盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂: " + e.getMessage());
                }

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓忙虏隆忙聹聣猫垄芦猫掳聝莽聰篓
                verify(requestHandler, never()).handleRequest(any());

                // 茅陋聦猫炉聛氓聯聧氓潞聰忙虏隆忙聹聣猫垄芦氓聫聭茅聙?                verify(channelContext, never()).writeAndFlush(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠莽漏潞忙露聢忙聛?)
        void testHandleNullMessage() {
                // 氓聢聸氓禄潞莽漏潞忙露聢忙聛炉盲潞聥盲禄?                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(null)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉茂录聢盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂茂录?                assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓忙虏隆忙聹聣猫垄芦猫掳聝莽聰篓
                verify(requestHandler, never()).handleRequest(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲猫炉路忙卤聜氓陇聞莽聬聠氓聶篓氓录聜氓赂?)
        void testRequestHandlerException() {
                // 氓聢聸氓禄潞忙碌聥猫炉聲猫炉路忙卤聜
                FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nettyRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓忙聤聸氓聡潞氓录聜氓赂?                when(requestHandler.handleRequest(any(HttpRequestEx.class)))
                                .thenThrow(new RuntimeException("Request handler error"));

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉茂录聢盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂茂录?                assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 茅陋聦猫炉聛channel.isActive()猫垄芦猫掳聝莽聰篓茂录聢handleError盲赂颅猫掳聝莽聰篓茂录聣
                verify(channel, atLeastOnce()).isActive();
        }

        @Test
        @DisplayName("忙碌聥猫炉聲盲赂聧氓聬聦HTTP忙聳鹿忙鲁聲")
        void testDifferentHttpMethods() {
                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲GET猫炉路忙卤聜
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

                // 忙碌聥猫炉聲POST猫炉路忙卤聜
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

                // 忙碌聥猫炉聲PUT猫炉路忙卤聜
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

                // 忙碌聥猫炉聲DELETE猫炉路忙卤聜
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

                // 茅陋聦猫炉聛忙聣聙忙聹聣猫炉路忙卤聜茅聝陆猫垄芦氓陇聞莽聬?                verify(requestHandler, times(4)).handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(4)).writeAndFlush(mockResponse);
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓赂娄猫炉路忙卤聜氓陇麓莽職聞HTTP猫炉路忙卤聜")
        void testHttpRequestWithHeaders() {
                // 氓聢聸氓禄潞氓赂娄猫炉路忙卤聜氓陇麓莽職聞HTTP猫炉路忙卤聜
                FullHttpRequest request = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");
                request.headers().set("Content-Type", "application/json");
                request.headers().set("Authorization", "Bearer token");
                request.headers().set("User-Agent", "Test Client");

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(request)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("氓陇聞莽聬聠忙露聢忙聛炉忙聴露忙聤聸氓聡潞氓录聜氓赂? " + e.getMessage());
                        e.printStackTrace();
                        fail("氓陇聞莽聬聠忙露聢忙聛炉忙聴露盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂: " + e.getMessage());
                }

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 茅陋聦猫炉聛ctx.writeAndFlush猫垄芦猫掳聝莽聰?                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓聯聧氓潞聰氓炉鹿猫卤隆莽卤禄氓聻聥猫陆卢忙聧垄")
        void testResponseObjectTypeConversion() {
                // 氓聢聸氓禄潞忙碌聥猫炉聲猫炉路忙卤聜
                FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.GET,
                                "/test");

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(nettyRequest)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(mockResponse.getStatusCode()).thenReturn(200);
                when(mockResponse.getContentType()).thenReturn("application/json");
                when(mockResponse.getBody()).thenReturn("{\"message\":\"test\"}");
                when(mockResponse.getHeaders()).thenReturn(new java.util.HashMap<>());
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                httpMessageHandler.handleMessage(event);

                // 茅陋聦猫炉聛氓聫聭茅聙聛莽職聞忙聵炉FullHttpResponse氓炉鹿猫卤隆茂录聦猫聙聦盲赂聧忙聵炉HttpResponseEx氓炉鹿猫卤隆
                verify(channelContext, atLeastOnce()).writeAndFlush(argThat(obj -> {
                        if (obj instanceof io.netty.handler.codec.http.FullHttpResponse) {
                                io.netty.handler.codec.http.FullHttpResponse response = 
                                        (io.netty.handler.codec.http.FullHttpResponse) obj;
                                return response.status().code() == 200;
                        }
                        return false;
                }));
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓赂娄猫炉路忙卤聜盲陆聯莽職聞HTTP猫炉路忙卤聜")
        void testHttpRequestWithBody() {
                // 氓聢聸氓禄潞氓赂娄猫炉路忙卤聜盲陆聯莽職聞POST猫炉路忙卤聜
                ByteBuf content = Unpooled.copiedBuffer("{\"key\": \"value\"}", io.netty.util.CharsetUtil.UTF_8);
                FullHttpRequest request = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1,
                                HttpMethod.POST,
                                "/api/test",
                                content);
                request.headers().set("Content-Type", "application/json");
                request.headers().set("Content-Length", content.readableBytes());

                // 莽隆庐盲驴聺content盲赂聧盲赂潞null盲赂聰氓聫炉猫炉?                assertNotNull(request.content(), "Request content should not be null");
                assertTrue(request.content().readableBytes() > 0, "Request content should have readable bytes");

                // 氓聢聸氓禄潞莽陆聭莽禄聹忙露聢忙聛炉盲潞聥盲禄露
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                                .protocolType("http")
                                .message(request)
                                .channelContext(channelContext)
                                .messageType("HTTP_REQUEST")
                                .build();

                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
                try {
                        httpMessageHandler.handleMessage(event);
                } catch (Exception e) {
                        System.err.println("氓陇聞莽聬聠忙露聢忙聛炉忙聴露忙聤聸氓聡潞氓录聜氓赂? " + e.getMessage());
                        e.printStackTrace();
                        fail("氓陇聞莽聬聠忙露聢忙聛炉忙聴露盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂: " + e.getMessage());
                }

                // 茅陋聦猫炉聛猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
                verify(requestHandler).handleRequest(any(HttpRequestEx.class));

                // 茅陋聦猫炉聛ctx.writeAndFlush猫垄芦猫掳聝莽聰?                verify(channelContext, atLeastOnce()).writeAndFlush(any());
        }

        @Test
        @DisplayName("忙碌聥猫炉聲氓鹿露氓聫聭氓陇聞莽聬聠")
        void testConcurrentHandling() throws InterruptedException {
                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                int threadCount = 10;
                int requestsPerThread = 100;
                Thread[] threads = new Thread[threadCount];

                // 氓聢聸氓禄潞氓陇職盲赂陋莽潞驴莽篓聥氓鹿露氓聫聭氓陇聞莽聬聠猫炉路忙卤聜
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

                // 氓聬炉氓聤篓忙聣聙忙聹聣莽潞驴莽篓?                for (Thread thread : threads) {
                        thread.start();
                }

                // 莽颅聣氓戮聟忙聣聙忙聹聣莽潞驴莽篓聥氓庐聦忙聢?                for (Thread thread : threads) {
                        thread.join();
                }

                // 茅陋聦猫炉聛忙聣聙忙聹聣猫炉路忙卤聜茅聝陆猫垄芦氓陇聞莽聬?                verify(requestHandler, times(threadCount * requestsPerThread))
                                .handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(threadCount * requestsPerThread))
                                .writeAndFlush(mockResponse);
        }

        @Test
        @DisplayName("忙碌聥猫炉聲忙聙搂猫聝陆")
        void testPerformance() {
                // Mock猫炉路忙卤聜氓陇聞莽聬聠氓聶篓猫驴聰氓聸聻氓聯聧氓潞?                HttpResponseEx mockResponse = mock(HttpResponseEx.class);
                when(requestHandler.handleRequest(any(HttpRequestEx.class))).thenReturn(mockResponse);

                int requestCount = 10000;
                long startTime = System.currentTimeMillis();

                // 氓陇聞莽聬聠氓陇搂茅聡聫猫炉路忙卤聜
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

                // 茅陋聦猫炉聛忙聙搂猫聝陆茂录聢氓潞聰猫炉楼猫聝陆氓聹篓氓聬聢莽聬聠忙聴露茅聴麓氓聠聟氓陇聞莽聬聠10000盲赂陋猫炉路忙卤聜茂录聣
                assertTrue(duration < 5000, "氓陇聞莽聬聠10000盲赂陋猫炉路忙卤聜氓潞聰猫炉楼氓聹篓5莽搂聮氓聠聟氓庐聦忙聢聬");

                // 茅陋聦猫炉聛忙聣聙忙聹聣猫炉路忙卤聜茅聝陆猫垄芦氓陇聞莽聬?                verify(requestHandler, times(requestCount)).handleRequest(any(HttpRequestEx.class));
                verify(channelContext, times(requestCount)).writeAndFlush(mockResponse);
        }
}
