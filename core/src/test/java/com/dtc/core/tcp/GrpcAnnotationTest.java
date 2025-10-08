package com.dtc.core.tcp;

import com.dtc.core.tcp.proto.TcpServiceGrpc;
import com.dtc.core.tcp.proto.TcpServiceProto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.annotation.Generated;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 gRPC 生成的代码中的注解问题
 */
public class GrpcAnnotationTest {

    @Test
    @DisplayName("测试Generated注解")
    void testGeneratedAnnotation() {
        // 测试 TcpServiceGrpc 类是否存在
        assertNotNull(TcpServiceGrpc.class, "TcpServiceGrpc class should exist");

        // 测试 Generated 注解是否可用
        try {
            Class<?> generatedClass = Class.forName("javax.annotation.Generated");
            assertNotNull(generatedClass, "Generated annotation should be available");
        } catch (ClassNotFoundException e) {
            fail("javax.annotation.Generated should be available: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试TcpServiceGrpc方法")
    void testTcpServiceGrpcMethods() {
        // 测试 TcpServiceGrpc 的主要方法
        try {
            Method getProcessMessageMethod = TcpServiceGrpc.class.getMethod("getProcessMessageMethod");
            assertNotNull(getProcessMessageMethod, "getProcessMessageMethod should exist");
        } catch (NoSuchMethodException e) {
            fail("getProcessMessageMethod should exist: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试TcpServiceProto类")
    void testTcpServiceProtoClasses() {
        // 测试 TcpServiceProto 类是否存在
        assertNotNull(TcpServiceProto.class, "TcpServiceProto class should exist");

        // 测试内部类
        assertNotNull(TcpServiceProto.TcpRequest.class, "TcpRequest class should exist");
        assertNotNull(TcpServiceProto.TcpResponse.class, "TcpResponse class should exist");
    }

    @Test
    @DisplayName("测试Generated注解使用")
    void testGeneratedAnnotationUsage() {
        // 测试 Generated 注解的使用
        Generated annotation = TcpServiceGrpc.class.getAnnotation(Generated.class);
        if (annotation != null) {
            assertEquals("by gRPC proto compiler (version 1.50.2)", annotation.value(),
                    "Generated annotation value should match");
        }
    }
}
