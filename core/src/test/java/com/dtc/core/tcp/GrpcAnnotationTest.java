package com.dtc.core.tcp;

import com.dtc.core.tcp.proto.TcpServiceGrpc;
import com.dtc.core.tcp.proto.TcpServiceProto;
import org.junit.Test;

import javax.annotation.Generated;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * 测试 gRPC 生成的代码中的注解问题
 */
public class GrpcAnnotationTest {

    @Test
    public void testGeneratedAnnotation() {
        // 测试 TcpServiceGrpc 类是否存在
        assertNotNull("TcpServiceGrpc class should exist", TcpServiceGrpc.class);

        // 测试 Generated 注解是否可用
        try {
            Class<?> generatedClass = Class.forName("javax.annotation.Generated");
            assertNotNull("Generated annotation should be available", generatedClass);
        } catch (ClassNotFoundException e) {
            fail("javax.annotation.Generated should be available: " + e.getMessage());
        }
    }

    @Test
    public void testTcpServiceGrpcMethods() {
        // 测试 TcpServiceGrpc 的主要方法
        try {
            Method getProcessMessageMethod = TcpServiceGrpc.class.getMethod("getProcessMessageMethod");
            assertNotNull("getProcessMessageMethod should exist", getProcessMessageMethod);
        } catch (NoSuchMethodException e) {
            fail("getProcessMessageMethod should exist: " + e.getMessage());
        }
    }

    @Test
    public void testTcpServiceProtoClasses() {
        // 测试 TcpServiceProto 类是否存在
        assertNotNull("TcpServiceProto class should exist", TcpServiceProto.class);

        // 测试内部类
        assertNotNull("TcpRequest class should exist", TcpServiceProto.TcpRequest.class);
        assertNotNull("TcpResponse class should exist", TcpServiceProto.TcpResponse.class);
    }

    @Test
    public void testGeneratedAnnotationUsage() {
        // 测试 Generated 注解的使用
        Generated annotation = TcpServiceGrpc.class.getAnnotation(Generated.class);
        if (annotation != null) {
            assertEquals("Generated annotation value should match",
                    "by gRPC proto compiler (version 1.50.2)", annotation.value());
        }
    }
}
