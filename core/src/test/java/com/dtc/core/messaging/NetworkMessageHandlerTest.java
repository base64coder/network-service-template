package com.dtc.core.messaging;

import com.dtc.core.serialization.ProtobufSerializer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import com.google.protobuf.Message;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageHandler 氓聧聲氓聟聝忙碌聥猫炉聲
 */
public class NetworkMessageHandlerTest {

    @Mock
    private ProtobufSerializer serializer;

    @Mock
    private NetworkMessageQueue messageQueue;

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

    private NetworkMessageHandler messageHandler;

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
        messageHandler = new NetworkMessageHandler(serializer, messageQueue);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠Protobuf忙露聢忙聛炉")
    void testHandleProtobufMessage() {
        // 氓聢聸氓禄潞忙碌聥猫炉聲忙露聢忙聛炉
        Message testMessage = mock(Message.class);
        when(testMessage.getSerializedSize()).thenReturn(100);

        // Mock茅聵聼氓聢聴氓聫聭氓赂聝
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
        boolean result = messageHandler.handleMessage(testMessage);

        // 茅陋聦猫炉聛莽禄聯忙聻聹
        assertTrue(result, "氓潞聰猫炉楼忙聢聬氓聤聼氓陇聞莽聬聠Protobuf忙露聢忙聛炉");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠氓聨聼氓搂聥氓颅聴猫聤聜忙聲掳忙聧庐")
    void testHandleRawData() {
        // 氓聢聸氓禄潞忙碌聥猫炉聲忙聲掳忙聧庐
        byte[] testData = "test raw data".getBytes();

        // Mock茅聵聼氓聢聴氓聫聭氓赂聝
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 忙碌聥猫炉聲氓陇聞莽聬聠氓聨聼氓搂聥忙聲掳忙聧庐
        boolean result = messageHandler.handleRawData(testData);

        // 茅陋聦猫炉聛莽禄聯忙聻聹
        assertTrue(result, "氓潞聰猫炉楼忙聢聬氓聤聼氓陇聞莽聬聠氓聨聼氓搂聥氓颅聴猫聤聜忙聲掳忙聧庐");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠莽漏潞忙露聢忙聛?)
    void testHandleEmptyMessage() {
        // Mock茅聵聼氓聢聴氓聫聭氓赂聝
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 忙碌聥猫炉聲莽漏潞氓颅聴猫聤聜忙聲掳莽禄?        byte[] emptyData = new byte[0];
        boolean result = messageHandler.handleRawData(emptyData);
        assertTrue(result, "氓潞聰猫炉楼猫聝陆氓陇聞莽聬聠莽漏潞忙露聢忙聛炉");

        // 忙碌聥猫炉聲null忙露聢忙聛炉
        assertThrows(NullPointerException.class, () -> {
            messageHandler.handleRawData(null);
        });
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠氓陇搂忙露聢忙聛?)
    void testHandleLargeMessage() {
        // 氓聢聸氓禄潞氓陇搂忙露聢忙聛炉茂录聢1MB茂录?        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        // Mock茅聵聼氓聢聴氓聫聭氓赂聝
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 忙碌聥猫炉聲氓陇聞莽聬聠氓陇搂忙露聢忙聛?        boolean result = messageHandler.handleRawData(largeData);

        // 茅陋聦猫炉聛莽禄聯忙聻聹
        assertTrue(result, "氓潞聰猫炉楼猫聝陆氓陇聞莽聬聠氓陇搂忙露聢忙聛炉");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠氓陇職莽搂聧忙聲掳忙聧庐莽卤禄氓聻聥")
    void testHandleDifferentDataTypes() {
        // Mock茅聵聼氓聢聴氓聫聭氓赂聝
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 忙碌聥猫炉聲氓颅聴莽卢娄盲赂虏忙聲掳忙聧?        byte[] stringData = "Hello World".getBytes();
        assertTrue(messageHandler.handleRawData(stringData));

        // 忙碌聥猫炉聲JSON忙聲掳忙聧庐
        byte[] jsonData = "{\"key\": \"value\"}".getBytes();
        assertTrue(messageHandler.handleRawData(jsonData));

        // 忙碌聥猫炉聲盲潞聦猫驴聸氓聢露忙聲掳忙聧?        byte[] binaryData = { 0x00, 0x01, 0x02, 0x03, 0x04 };
        assertTrue(messageHandler.handleRawData(binaryData));

        // 茅陋聦猫炉聛忙聣聙忙聹聣忙露聢忙聛炉茅聝陆猫垄芦氓聫聭氓赂?        verify(messageQueue, times(3)).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聵聼氓聢聴氓聫聭氓赂聝氓陇卤猫麓楼")
    void testQueuePublishFailure() {
        // Mock茅聵聼氓聢聴氓聫聭氓赂聝氓陇卤猫麓楼
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(false);

        // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
        byte[] testData = "test data".getBytes();
        boolean result = messageHandler.handleRawData(testData);

        // 茅陋聦猫炉聛莽禄聯忙聻聹
        assertFalse(result, "茅聵聼氓聢聴氓聫聭氓赂聝氓陇卤猫麓楼忙聴露氓潞聰猫炉楼猫驴聰氓聸聻false");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓陇聞莽聬聠氓录聜氓赂赂忙聝聟氓聠碌")
    void testHandleException() {
        // Mock茅聵聼氓聢聴忙聤聸氓聡潞氓录聜氓赂赂
        when(messageQueue.publish(any(NetworkMessageEvent.class)))
                .thenThrow(new RuntimeException("Queue error"));

        // 忙碌聥猫炉聲氓陇聞莽聬聠忙露聢忙聛炉
        byte[] testData = "test data".getBytes();
        boolean result = messageHandler.handleRawData(testData);

        // 茅陋聦猫炉聛莽禄聯忙聻聹
        assertFalse(result, "氓录聜氓赂赂忙聝聟氓聠碌盲赂聥氓潞聰猫炉楼猫驴聰氓聸聻false");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉")
    void testGetStats() {
        // 氓陇聞莽聬聠盲赂聙盲潞聸忙露聢忙聛?        byte[] testData1 = "test data 1".getBytes();
        byte[] testData2 = "test data 2".getBytes();

        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        messageHandler.handleRawData(testData1);
        messageHandler.handleRawData(testData2);

        // 猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();

        // 茅陋聦猫炉聛莽禄聼猫庐隆盲驴隆忙聛炉
        assertNotNull(stats, "莽禄聼猫庐隆盲驴隆忙聛炉盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertTrue(stats.getReceivedCount() >= 0, "忙聨楼忙聰露猫庐隆忙聲掳氓潞聰猫炉楼氓陇搂盲潞聨莽颅聣盲潞聨0");
        assertTrue(stats.getForwardedCount() >= 0, "猫陆卢氓聫聭猫庐隆忙聲掳氓潞聰猫炉楼氓陇搂盲潞聨莽颅聣盲潞聨0");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲莽禄聼猫庐隆盲驴隆忙聛炉莽卤?)
    void testHandlerStats() {
        // 氓聢聸氓禄潞莽禄聼猫庐隆盲驴隆忙聛炉
        NetworkMessageHandler.HandlerStats stats = new NetworkMessageHandler.HandlerStats(10, 8);

        // 茅陋聦猫炉聛莽禄聼猫庐隆盲驴隆忙聛炉
        assertEquals(10, stats.getReceivedCount());
        assertEquals(8, stats.getForwardedCount());

        // 忙碌聥猫炉聲toString忙聳鹿忙鲁聲
        String statsString = stats.toString();
        assertNotNull(statsString);
        assertTrue(statsString.contains("received=10"));
        assertTrue(statsString.contains("forwarded=8"));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓鹿露氓聫聭氓陇聞莽聬聠")
    void testConcurrentHandling() throws InterruptedException {
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 氓聢聸氓禄潞氓陇職盲赂陋莽潞驴莽篓聥氓鹿露氓聫聭氓陇聞莽聬聠忙露聢忙聛炉
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    byte[] testData = ("concurrent test " + j).getBytes();
                    messageHandler.handleRawData(testData);
                }
            });
        }

        // 氓聬炉氓聤篓忙聣聙忙聹聣莽潞驴莽篓?        for (Thread thread : threads) {
            thread.start();
        }

        // 莽颅聣氓戮聟忙聣聙忙聹聣莽潞驴莽篓聥氓庐聦忙聢?        for (Thread thread : threads) {
            thread.join();
        }

        // 茅陋聦猫炉聛莽禄聼猫庐隆盲驴隆忙聛炉
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();
        assertTrue(stats.getReceivedCount() >= threadCount * messagesPerThread);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聙搂猫聝陆")
    void testPerformance() {
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 氓陇聞莽聬聠氓陇搂茅聡聫忙露聢忙聛炉
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("performance test " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 茅陋聦猫炉聛忙聙搂猫聝陆茂录聢氓潞聰猫炉楼猫聝陆氓聹?莽搂聮氓聠聟氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉茂录聣
        assertTrue(duration < 1000, "氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉氓潞聰猫炉楼氓聹篓1莽搂聮氓聠聟氓庐聦忙聢聬");

        // 茅陋聦猫炉聛莽禄聼猫庐隆盲驴隆忙聛炉
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();
        assertTrue(stats.getReceivedCount() >= messageCount);
    }
}
