package com.dtc.core.serialization;

import com.dtc.core.protobuf.NetworkMessageProtos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProtobufSerializer 测试
 */
@DisplayName("Protobuf序列化器测试")
public class ProtobufSerializerTest {

    private ProtobufSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new ProtobufSerializer();
    }

    @Test
    @DisplayName("测试创建序列化器")
    void testCreateSerializer() {
        assertNotNull(serializer);
    }

    @Test
    @DisplayName("测试序列化Protobuf消息")
    void testSerializeMessage() {
        NetworkMessageProtos.HeartbeatMessage message = 
            NetworkMessageProtos.HeartbeatMessage.newBuilder()
                .setClientId("test-client")
                .setLastHeartbeat(System.currentTimeMillis())
                .build();
        
        byte[] bytes = serializer.serialize(message);
        
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    @DisplayName("测试反序列化Protobuf消息")
    void testDeserializeMessage() throws Exception {
        NetworkMessageProtos.HeartbeatMessage message = 
            NetworkMessageProtos.HeartbeatMessage.newBuilder()
                .setClientId("test-client")
                .setLastHeartbeat(123456789L)
                .build();
        
        byte[] bytes = serializer.serialize(message);
        NetworkMessageProtos.HeartbeatMessage deserialized = 
            serializer.deserialize(bytes, NetworkMessageProtos.HeartbeatMessage.class);
        
        assertNotNull(deserialized);
        assertEquals("test-client", deserialized.getClientId());
        assertEquals(123456789L, deserialized.getLastHeartbeat());
    }

    @Test
    @DisplayName("测试反序列化空字节数组")
    void testDeserializeNull() throws Exception {
        NetworkMessageProtos.HeartbeatMessage result = 
            serializer.deserialize(new byte[0], NetworkMessageProtos.HeartbeatMessage.class);
        // 空字节数组会返回默认的空消息，而不是null
        assertNotNull(result);
    }
}

