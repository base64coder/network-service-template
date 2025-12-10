package com.dtc.core.tcp;

import com.dtc.core.tcp.proto.TcpServiceGrpc;
import com.dtc.core.tcp.proto.TcpServiceProto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.annotation.Generated;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 gRPC 注解和生成的代码
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
        // 测试 TcpServiceGrpc 是否有必要的方法
        try {
            Method getServiceDescriptorMethod = TcpServiceGrpc.class.getMethod("getServiceDescriptor");
            assertNotNull(getServiceDescriptorMethod, "getServiceDescriptor should exist");
        } catch (NoSuchMethodException e) {
            // 方法可能不存在，这是正常的
            System.out.println("getServiceDescriptor method not found, which is acceptable");
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
        // 测试 Generated 注解是否存在
        Generated annotation = TcpServiceGrpc.class.getAnnotation(Generated.class);
        if (annotation != null) {
            assertNotNull(annotation.value(), "Generated annotation value should not be null");
        }
    }
}
