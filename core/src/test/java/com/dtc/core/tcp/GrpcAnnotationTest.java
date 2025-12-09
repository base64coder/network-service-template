package com.dtc.core.tcp;

import com.dtc.core.network.tcp.proto.TcpServiceGrpc;
import com.dtc.core.network.tcp.proto.TcpServiceProto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.annotation.Generated;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * å¨´å¬­ç¯ gRPC é¢ç¸åé¨åªå¬é®ä½·èé¨å¬æçï½æ£¶æ£°
     */
public class GrpcAnnotationTest {

    @Test
    @DisplayName("å¨´å¬­ç¯Generatedå¨ã¨Ð")
    void testGeneratedAnnotation() {
        // å¨´å¬­ç¯ TcpServiceGrpc ç»«ç»æ§¸éï¹ç¨é¦?        assertNotNull(TcpServiceGrpc.class, "TcpServiceGrpc class should exist");

        // å¨´å¬­ç¯ Generated å¨ã¨Ðéîæéîæ¤
        try {
            Class<?> generatedClass = Class.forName("javax.annotation.Generated");
            assertNotNull(generatedClass, "Generated annotation should be available");
        } catch (ClassNotFoundException e) {
            fail("javax.annotation.Generated should be available: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("å¨´å¬­ç¯TcpServiceGrpcéè§ç¡¶")
    void testTcpServiceGrpcMethods() {
        // å¨´å¬­ç¯ TcpServiceGrpc é¨åªå¯çä½¹æå¨?        try {
            Method getProcessMessageMethod = TcpServiceGrpc.class.getMethod("getProcessMessageMethod");
            assertNotNull(getProcessMessageMethod, "getProcessMessageMethod should exist");
        } catch (NoSuchMethodException e) {
            fail("getProcessMessageMethod should exist: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("å¨´å¬­ç¯TcpServiceProtoç»«?)
    void testTcpServiceProtoClasses() {
        // å¨´å¬­ç¯ TcpServiceProto ç»«ç»æ§¸éï¹ç¨é¦?        assertNotNull(TcpServiceProto.class, "TcpServiceProto class should exist");

        // å¨´å¬­ç¯éå´å´ç»«?        assertNotNull(TcpServiceProto.TcpRequest.class, "TcpRequest class should exist");
        assertNotNull(TcpServiceProto.TcpResponse.class, "TcpResponse class should exist");
    }

    @Test
    @DisplayName("å¨´å¬­ç¯Generatedå¨ã¨Ðæµ£è·¨æ¤")
    void testGeneratedAnnotationUsage() {
        // å¨´å¬­ç¯ Generated å¨ã¨Ðé¨åªå¨é¢?        Generated annotation = TcpServiceGrpc.class.getAnnotation(Generated.class);
        if (annotation != null) {
            assertEquals("by gRPC proto compiler (version 1.50.2)", annotation.value(),
                    "Generated annotation value should match");
        }
    }
}
