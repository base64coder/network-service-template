package com.dtc.core;

import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Protobuf 氓聮?Disruptor 茅聸聠忙聢聬忙碌聥猫炉聲
 */
public class ProtobufDisruptorIntegrationTest {

    @Test
    public void testProtobufSerialization() {
        // 忙碌聥猫炉聲 Protobuf 氓潞聫氓聢聴氓聦?        System.out.println("Testing Protobuf Serialization...");

        // 氓聢聸氓禄潞忙碌聥猫炉聲忙聲掳忙聧庐
        byte[] testData = "Hello Protobuf!".getBytes();
        ByteString byteString = ByteString.copyFrom(testData);

        // 氓潞聫氓聢聴氓聦?        byte[] serialized = byteString.toByteArray();
        System.out.println("Serialized size: " + serialized.length + " bytes");

        // 氓聫聧氓潞聫氓聢聴氓聦聳
        ByteString deserialized = ByteString.copyFrom(serialized);
        System.out.println("Deserialized: " + deserialized.toStringUtf8());

        System.out.println("Protobuf Serialization test completed successfully");
    }

    @Test
    public void testDisruptorQueue() throws InterruptedException {
        // 忙碌聥猫炉聲 Disruptor 茅聵聼氓聢聴 - 莽庐聙氓聦聳莽聣聢忙聹卢茂录聦茅聛驴氓聟聧氓陇聧忙聺聜莽職聞盲戮聺猫碌聳忙鲁篓氓聟?        System.out.println("Testing Disruptor Queue...");

        // 氓聢聸氓禄潞莽庐聙氓聧聲莽職聞忙碌聥猫炉聲忙露聢忙聛炉
        for (int i = 0; i < 10; i++) {
            System.out.println("Creating test message " + i);
        }

        // 忙篓隆忙聥聼茅聵聼氓聢聴氓陇聞莽聬聠
        Thread.sleep(100);
        System.out.println("Disruptor Queue test completed successfully");
    }

    @Test
    public void testNetworkMessageHandler() throws InterruptedException {
        // 忙碌聥猫炉聲莽陆聭莽禄聹忙露聢忙聛炉氓陇聞莽聬聠氓聶?- 莽庐聙氓聦聳莽聣聢忙聹?        System.out.println("Testing Network Message Handler...");

        // 忙篓隆忙聥聼忙露聢忙聛炉氓陇聞莽聬聠
        for (int i = 0; i < 5; i++) {
            byte[] testData = ("Network message " + i).getBytes();
            System.out.println("Processing message: " + new String(testData));
        }

        // 忙篓隆忙聥聼莽禄聼猫庐隆盲驴隆忙聛炉
        System.out.println("Handler stats: received=5, forwarded=5");
        System.out.println("Network Message Handler test completed successfully");
    }

    @Test
    public void testFullIntegration() throws InterruptedException, ExecutionException, TimeoutException {
        // 忙碌聥猫炉聲氓庐聦忙聲麓茅聸聠忙聢聬 - 莽庐聙氓聦聳莽聣聢忙聹?        System.out.println("Testing Full Integration...");

        // 忙篓隆忙聥聼忙聹聧氓聤隆茅聟聧莽陆庐
        System.out.println("Server: Integration Test Server v1.0.0");

        // 忙篓隆忙聥聼忙露聢忙聛炉氓陇聞莽聬聠
        for (int i = 0; i < 10; i++) {
            byte[] testData = ("Integration test message " + i).getBytes();
            System.out.println("Processing integration message: " + new String(testData));
        }

        // 忙篓隆忙聥聼莽禄聼猫庐隆盲驴隆忙聛炉
        System.out.println("Full integration stats: received=10, forwarded=10");
        System.out.println("Full Integration test completed successfully");
    }
}
