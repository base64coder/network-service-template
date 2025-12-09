package com.dtc.core.messaging;

import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageQueue 氓聧聲氓聟聝忙碌聥猫炉聲
 */
public class NetworkMessageQueueTest {

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
    private NetworkMessageConsumer consumer;

    private NetworkMessageQueue messageQueue;

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
        messageQueue = new NetworkMessageQueue(consumer);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聵聼氓聢聴氓聬炉氓聤篓氓聮聦氓聛聹忙颅?)
    void testQueueStartAndStop() {
        // 忙碌聥猫炉聲氓聬炉氓聤篓
        assertDoesNotThrow(() -> messageQueue.start());

        // 忙碌聥猫炉聲氓聛聹忙颅垄
        assertDoesNotThrow(() -> messageQueue.stop());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢忙聛炉氓聫聭氓赂聝")
    @Timeout(5)
    void testMessagePublish() throws InterruptedException {
        // 氓聬炉氓聤篓茅聵聼氓聢聴
        messageQueue.start();

        // 氓聢聸氓禄潞忙碌聥猫炉聲忙露聢忙聛炉
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("test")
                .message("test message")
                .messageType("TEST_MESSAGE")
                .build();

        // 氓聫聭氓赂聝忙露聢忙聛炉
        boolean published = messageQueue.publish(event);
        assertTrue(published, "忙露聢忙聛炉氓潞聰猫炉楼忙聢聬氓聤聼氓聫聭氓赂聝");

        // 莽颅聣氓戮聟氓陇聞莽聬聠
        Thread.sleep(100);

        // 氓聛聹忙颅垄茅聵聼氓聢聴
        messageQueue.stop();
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聵聼氓聢聴莽聤露忙聙?)
    void testQueueStatus() {
        // 氓聬炉氓聤篓氓聣聧莽聤露忙聙?        NetworkMessageQueue.QueueStatus status = messageQueue.getStatus();
        assertNotNull(status);

        // 氓聬炉氓聤篓茅聵聼氓聢聴
        messageQueue.start();

        // 氓聬炉氓聤篓氓聬聨莽聤露忙聙?        status = messageQueue.getStatus();
        assertNotNull(status);

        // 氓聛聹忙颅垄茅聵聼氓聢聴
        messageQueue.stop();
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅芦聵氓鹿露氓聫聭忙露聢忙聛炉氓聫聭氓赂?)
    @Timeout(10)
    void testConcurrentMessagePublish() throws InterruptedException {
        messageQueue.start();

        int messageCount = 1000;
        CountDownLatch latch = new CountDownLatch(messageCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // 氓鹿露氓聫聭氓聫聭氓赂聝忙露聢忙聛炉
        for (int i = 0; i < messageCount; i++) {
            new Thread(() -> {
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                        .protocolType("test")
                        .message("concurrent test message")
                        .messageType("CONCURRENT_TEST")
                        .build();

                if (messageQueue.publish(event)) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        // 莽颅聣氓戮聟忙聣聙忙聹聣忙露聢忙聛炉氓陇聞莽聬聠氓庐聦忙聢?        assertTrue(latch.await(5, TimeUnit.SECONDS), "忙聣聙忙聹聣忙露聢忙聛炉氓潞聰猫炉楼氓聹篓5莽搂聮氓聠聟氓陇聞莽聬聠氓庐聦忙聢聬");

        // 茅陋聦猫炉聛忙聢聬氓聤聼氓聫聭氓赂聝莽職聞忙露聢忙聛炉忙聲掳茅聡?        assertTrue(successCount.get() > 0, "氓潞聰猫炉楼忙聹聣忙露聢忙聛炉忙聢聬氓聤聼氓聫聭氓赂?);

        messageQueue.stop();
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聵聼氓聢聴忙禄隆忙聴露莽職聞氓陇聞莽聬?)
    void testQueueFullHandling() {
        messageQueue.start();

        // 氓聫聭氓赂聝氓陇搂茅聡聫忙露聢忙聛炉莽聸麓氓聢掳茅聵聼氓聢聴忙禄?        int publishedCount = 0;
        for (int i = 0; i < 10000; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("test")
                    .message("test message " + i)
                    .messageType("TEST_MESSAGE")
                    .build();

            if (messageQueue.publish(event)) {
                publishedCount++;
            } else {
                break; // 茅聵聼氓聢聴忙禄隆茂录聦氓聛聹忙颅垄氓聫聭氓赂聝
            }
        }

        assertTrue(publishedCount > 0, "氓潞聰猫炉楼忙聹聣盲赂聙盲潞聸忙露聢忙聛炉忙聢聬氓聤聼氓聫聭氓赂?);

        messageQueue.stop();
    }

    @Test
    @DisplayName("忙碌聥猫炉聲盲赂聧氓聬聦氓聧聫猫庐庐莽卤禄氓聻聥莽職聞忙露聢忙聛?)
    void testDifferentProtocolMessages() {
        messageQueue.start();

        // 忙碌聥猫炉聲HTTP忙露聢忙聛炉
        NetworkMessageEvent httpEvent = NetworkMessageEvent.builder()
                .protocolType("http")
                .message("HTTP message")
                .messageType("HTTP_REQUEST")
                .build();
        assertTrue(messageQueue.publish(httpEvent));

        // 忙碌聥猫炉聲WebSocket忙露聢忙聛炉
        NetworkMessageEvent wsEvent = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message("WebSocket message")
                .messageType("WEBSOCKET_FRAME")
                .build();
        assertTrue(messageQueue.publish(wsEvent));

        // 忙碌聥猫炉聲MQTT忙露聢忙聛炉
        NetworkMessageEvent mqttEvent = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .messageType("MQTT_MESSAGE")
                .build();
        assertTrue(messageQueue.publish(mqttEvent));

        messageQueue.stop();
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢忙聛炉盲潞聥盲禄露忙聻聞氓禄潞氓聶?)
    void testNetworkMessageEventBuilder() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("test")
                .clientId("test-client")
                .message("test message")
                .messageType("TEST_MESSAGE")
                .messageSize(12)
                .isRequest(true)
                .priority(1)
                .build();

        assertNotNull(event);
        assertEquals("test", event.getProtocolType());
        assertEquals("test-client", event.getClientId());
        assertEquals("test message", event.getMessage());
        assertEquals("TEST_MESSAGE", event.getMessageType());
        assertEquals(12, event.getMessageSize());
        assertTrue(event.isRequest());
        assertEquals(1, event.getPriority());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聵聼氓聢聴忙聙搂猫聝陆")
    @Timeout(10)
    void testQueuePerformance() throws InterruptedException {
        messageQueue.start();

        long startTime = System.currentTimeMillis();
        int messageCount = 10000;

        // 氓聫聭氓赂聝氓陇搂茅聡聫忙露聢忙聛炉
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("test")
                    .message("performance test message " + i)
                    .messageType("PERFORMANCE_TEST")
                    .build();

            messageQueue.publish(event);
        }

        // 莽颅聣氓戮聟氓陇聞莽聬聠氓庐聦忙聢聬
        Thread.sleep(1000);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 茅陋聦猫炉聛忙聙搂猫聝陆茂录聢氓潞聰猫炉楼猫聝陆氓聹篓氓聬聢莽聬聠忙聴露茅聴麓氓聠聟氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉茂录聣
        assertTrue(duration < 5000, "氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉氓潞聰猫炉楼氓聹篓5莽搂聮氓聠聟氓庐聦忙聢聬");

        messageQueue.stop();
    }
}
