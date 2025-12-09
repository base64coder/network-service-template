package com.dtc.core.messaging;

import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
 * NetworkMessageConsumer 氓聧聲氓聟聝忙碌聥猫炉聲
 */
public class NetworkMessageConsumerTest {

    @Mock
    private StatisticsCollector statisticsCollector;

    @Mock
    private HttpMessageHandler httpMessageHandler;

    @Mock
    private WebSocketMessageHandler webSocketMessageHandler;

    @Mock
    private MqttMessageHandler mqttMessageHandler;

    @Mock
    private TcpMessageHandler tcpMessageHandler;

    @Mock
    private CustomMessageHandler customMessageHandler;

    @Mock
    private ChannelHandlerContext channelContext;

    private NetworkMessageConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new NetworkMessageConsumer(
                statisticsCollector,
                httpMessageHandler,
                webSocketMessageHandler,
                mqttMessageHandler,
                tcpMessageHandler,
                customMessageHandler);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿HTTP忙露聢忙聛炉")
    void testConsumeHttpMessage() {
        // 氓聢聸氓禄潞HTTP忙露聢忙聛炉盲潞聥盲禄露
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉
        consumer.consume(event, 1L, true);

        // 茅陋聦猫炉聛HTTP氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
        verify(httpMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿WebSocket忙露聢忙聛炉")
    void testConsumeWebSocketMessage() {
        // 氓聢聸氓禄潞WebSocket忙露聢忙聛炉盲潞聥盲禄露
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message(mock(TextWebSocketFrame.class))
                .channelContext(channelContext)
                .messageType("WEBSOCKET_FRAME")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉
        consumer.consume(event, 1L, true);

        // 茅陋聦猫炉聛WebSocket氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
        verify(webSocketMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿MQTT忙露聢忙聛炉")
    void testConsumeMqttMessage() {
        // 氓聢聸氓禄潞MQTT忙露聢忙聛炉盲潞聥盲禄露
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .channelContext(channelContext)
                .messageType("MQTT_MESSAGE")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉
        consumer.consume(event, 1L, true);

        // 茅陋聦猫炉聛MQTT氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
        verify(mqttMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿TCP忙露聢忙聛炉")
    void testConsumeTcpMessage() {
        // 氓聢聸氓禄潞TCP忙露聢忙聛炉盲潞聥盲禄露
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes("TCP message".getBytes());

        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("tcp")
                .message(byteBuf)
                .channelContext(channelContext)
                .messageType("TCP_MESSAGE")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉
        consumer.consume(event, 1L, true);

        // 茅陋聦猫炉聛TCP氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
        verify(tcpMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿猫聡陋氓庐職盲鹿聣氓聧聫猫庐庐忙露聢忙聛?)
    void testConsumeCustomMessage() {
        // 氓聢聸氓禄潞猫聡陋氓庐職盲鹿聣氓聧聫猫庐庐忙露聢忙聛炉盲潞聥盲禄?        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("custom")
                .message("Custom message")
                .channelContext(channelContext)
                .messageType("CUSTOM_MESSAGE")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉
        consumer.consume(event, 1L, true);

        // 茅陋聦猫炉聛猫聡陋氓庐職盲鹿聣氓聧聫猫庐庐氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰?        verify(customMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿忙聹陋莽聼楼氓聧聫猫庐庐忙露聢忙聛炉")
    void testConsumeUnknownProtocolMessage() {
        // 氓聢聸氓禄潞忙聹陋莽聼楼氓聧聫猫庐庐忙露聢忙聛炉盲潞聥盲禄露
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("unknown")
                .message("Unknown message")
                .channelContext(channelContext)
                .messageType("UNKNOWN_MESSAGE")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉茂录聢盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂茂录?        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));

        // 茅陋聦猫炉聛忙虏隆忙聹聣氓陇聞莽聬聠氓聶篓猫垄芦猫掳聝莽聰篓
        verify(httpMessageHandler, never()).handleMessage(any());
        verify(webSocketMessageHandler, never()).handleMessage(any());
        verify(mqttMessageHandler, never()).handleMessage(any());
        verify(tcpMessageHandler, never()).handleMessage(any());
        verify(customMessageHandler, never()).handleMessage(any());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢猫麓鹿莽漏潞忙露聢忙聛?)
    void testConsumeNullMessage() {
        // 氓聢聸氓禄潞莽漏潞忙露聢忙聛炉盲潞聥盲禄?        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(null)
                .channelContext(channelContext)
                .messageType("NULL_MESSAGE")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉茂录聢盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂茂录?        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠氓聶篓氓录聜氓赂赂氓陇聞莽聬?)
    void testHandlerExceptionHandling() {
        // Mock氓陇聞莽聬聠氓聶篓忙聤聸氓聡潞氓录聜氓赂?        doThrow(new RuntimeException("Handler error"))
                .when(httpMessageHandler).handleMessage(any());

        // 氓聢聸氓禄潞HTTP忙露聢忙聛炉盲潞聥盲禄露
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();

        // 忙露聢猫麓鹿忙露聢忙聛炉茂录聢盲赂聧氓潞聰猫炉楼忙聤聸氓聡潞氓录聜氓赂赂茂录?        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓鹿露氓聫聭忙露聢猫麓鹿")
    void testConcurrentConsumption() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 氓聢聸氓禄潞氓陇職盲赂陋莽潞驴莽篓聥氓鹿露氓聫聭忙露聢猫麓鹿忙露聢忙聛炉
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    NetworkMessageEvent event = NetworkMessageEvent.builder()
                            .protocolType("http")
                            .message(mock(FullHttpRequest.class))
                            .channelContext(channelContext)
                            .messageType("HTTP_REQUEST")
                            .build();

                    consumer.consume(event, 1L, true);
                }
            });
        }

        // 氓聬炉氓聤篓忙聣聙忙聹聣莽潞驴莽篓?        for (Thread thread : threads) {
            thread.start();
        }

        // 莽颅聣氓戮聟忙聣聙忙聹聣莽潞驴莽篓聥氓庐聦忙聢?        for (Thread thread : threads) {
            thread.join();
        }

        // 茅陋聦猫炉聛忙聣聙忙聹聣忙露聢忙聛炉茅聝陆猫垄芦氓陇聞莽聬?        verify(httpMessageHandler, times(threadCount * messagesPerThread))
                .handleMessage(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲盲赂聧氓聬聦忙露聢忙聛炉莽卤禄氓聻聥莽職聞氓陇聞莽聬?)
    void testDifferentMessageTypes() {
        // 忙碌聥猫炉聲HTTP忙露聢忙聛炉
        NetworkMessageEvent httpEvent = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();
        consumer.consume(httpEvent, 1L, true);
        verify(httpMessageHandler).handleMessage(httpEvent);

        // 忙碌聥猫炉聲WebSocket忙露聢忙聛炉
        NetworkMessageEvent wsEvent = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message(mock(TextWebSocketFrame.class))
                .channelContext(channelContext)
                .messageType("WEBSOCKET_FRAME")
                .build();
        consumer.consume(wsEvent, 1L, true);
        verify(webSocketMessageHandler).handleMessage(wsEvent);

        // 忙碌聥猫炉聲MQTT忙露聢忙聛炉
        NetworkMessageEvent mqttEvent = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .channelContext(channelContext)
                .messageType("MQTT_MESSAGE")
                .build();
        consumer.consume(mqttEvent, 1L, true);
        verify(mqttMessageHandler).handleMessage(mqttEvent);

        // 忙碌聥猫炉聲TCP忙露聢忙聛炉
        ByteBuf tcpData = Unpooled.buffer();
        tcpData.writeBytes("TCP data".getBytes());
        NetworkMessageEvent tcpEvent = NetworkMessageEvent.builder()
                .protocolType("tcp")
                .message(tcpData)
                .channelContext(channelContext)
                .messageType("TCP_MESSAGE")
                .build();
        consumer.consume(tcpEvent, 1L, true);
        verify(tcpMessageHandler).handleMessage(tcpEvent);

        // 忙碌聥猫炉聲猫聡陋氓庐職盲鹿聣氓聧聫猫庐庐忙露聢忙聛?        NetworkMessageEvent customEvent = NetworkMessageEvent.builder()
                .protocolType("custom")
                .message("Custom message")
                .channelContext(channelContext)
                .messageType("CUSTOM_MESSAGE")
                .build();
        consumer.consume(customEvent, 1L, true);
        verify(customMessageHandler).handleMessage(customEvent);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聙搂猫聝陆")
    void testPerformance() {
        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 氓陇聞莽聬聠氓陇搂茅聡聫忙露聢忙聛炉
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("http")
                    .message(mock(FullHttpRequest.class))
                    .channelContext(channelContext)
                    .messageType("HTTP_REQUEST")
                    .build();

            consumer.consume(event, 1L, true);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 茅陋聦猫炉聛忙聙搂猫聝陆茂录聢氓潞聰猫炉楼猫聝陆氓聹?莽搂聮氓聠聟氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉茂录聣
        assertTrue(duration < 1000, "氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉氓潞聰猫炉楼氓聹篓1莽搂聮氓聠聟氓庐聦忙聢聬");

        // 茅陋聦猫炉聛忙聣聙忙聹聣忙露聢忙聛炉茅聝陆猫垄芦氓陇聞莽聬?        verify(httpMessageHandler, times(messageCount)).handleMessage(any(NetworkMessageEvent.class));
    }
}
