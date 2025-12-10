package com.dtc.core.network.netty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NettyBootstrap 测试
 */
@DisplayName("Netty启动器测试")
public class NettyBootstrapTest {

    @Mock
    private NettyServer mockNettyServer;

    private NettyBootstrap bootstrap;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        bootstrap = new NettyBootstrap(mockNettyServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @DisplayName("测试启动服务器")
    void testStartServer() throws Exception {
        doNothing().when(mockNettyServer).start();
        
        CompletableFuture<Void> future = bootstrap.startServer();
        
        assertNotNull(future);
        future.get(); // 等待完成
        
        verify(mockNettyServer, times(1)).start();
        assertTrue(bootstrap.isStarted());
    }

    @Test
    @DisplayName("测试重复启动服务器")
    void testStartServerTwice() throws Exception {
        doNothing().when(mockNettyServer).start();
        
        CompletableFuture<Void> future1 = bootstrap.startServer();
        future1.get();
        
        CompletableFuture<Void> future2 = bootstrap.startServer();
        future2.get();
        
        // 应该只启动一次
        verify(mockNettyServer, times(1)).start();
    }

    @Test
    @DisplayName("测试停止服务器")
    void testStopServer() throws Exception {
        doNothing().when(mockNettyServer).start();
        doNothing().when(mockNettyServer).stop();
        
        bootstrap.startServer().get();
        assertTrue(bootstrap.isStarted());
        
        CompletableFuture<Void> future = bootstrap.stopServer();
        future.get();
        
        verify(mockNettyServer, times(1)).stop();
        assertFalse(bootstrap.isStarted());
    }

    @Test
    @DisplayName("测试停止未启动的服务器")
    void testStopServerNotStarted() throws Exception {
        CompletableFuture<Void> future = bootstrap.stopServer();
        future.get();
        
        verify(mockNettyServer, never()).stop();
        assertFalse(bootstrap.isStarted());
    }

    @Test
    @DisplayName("测试启动服务器失败")
    void testStartServerFailure() throws Exception {
        doThrow(new RuntimeException("启动失败")).when(mockNettyServer).start();
        
        CompletableFuture<Void> future = bootstrap.startServer();
        
        assertThrows(Exception.class, future::get);
        assertFalse(bootstrap.isStarted());
    }

    @Test
    @DisplayName("测试检查启动状态")
    void testIsStarted() throws Exception {
        assertFalse(bootstrap.isStarted());
        
        doNothing().when(mockNettyServer).start();
        bootstrap.startServer().get();
        
        assertTrue(bootstrap.isStarted());
    }
}

