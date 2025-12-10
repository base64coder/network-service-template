package com.dtc.core.network.http;

import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HttpServer 测试
 */
@DisplayName("HTTP服务器测试")
public class HttpServerTest {

    @Mock
    private StatisticsCollector mockStatisticsCollector;

    @Mock
    private ChannelHandlerContext mockContext;

    private HttpServer httpServer;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        httpServer = new HttpServer(mockStatisticsCollector);
    }

    @Test
    @DisplayName("测试创建HttpServer实例")
    void testCreateHttpServer() {
        assertNotNull(httpServer);
    }

    @Test
    @DisplayName("测试channelActive")
    void testChannelActive() throws Exception {
        httpServer.channelActive(mockContext);
        
        verify(mockStatisticsCollector, times(1)).onConnectionEstablished();
    }

    @Test
    @DisplayName("测试channelInactive")
    void testChannelInactive() throws Exception {
        httpServer.channelInactive(mockContext);
        
        verify(mockStatisticsCollector, times(1)).onConnectionClosed();
    }

    @Test
    @DisplayName("测试异常处理")
    void testExceptionCaught() throws Exception {
        Throwable error = new RuntimeException("test error");
        
        assertDoesNotThrow(() -> httpServer.exceptionCaught(mockContext, error));
    }
}

