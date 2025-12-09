package com.dtc.tcp.handler;

import com.dtc.core.network.tcp.TcpMessageHelper;
import com.dtc.annotations.web.MessageHandler;
import com.dtc.annotations.web.TcpHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * TCPå¨å ä¼æ¾¶å­æé£ã§ãæ¸?
 * å©æãæ¿¡åç¶æµ£è·¨æ¤å¨ã¨Ðæ¤¹åå§©é¨å¬æå¯®å¿î©éå¡CPå¨å ä¼
 * 
 * @author Network Service Template
 */
@MessageHandler(protocol = "TCP")
@Singleton
public class ExampleTcpHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ExampleTcpHandler.class);
    
    private final TcpMessageHelper tcpMessageHelper;
    
    @Inject
    public ExampleTcpHandler(TcpMessageHelper tcpMessageHelper) {
        this.tcpMessageHelper = tcpMessageHelper;
    }
    
    /**
     * æ¾¶å­æpingå¨å ä¼
     */
    @TcpHandler("ping")
    public void handlePing(ChannelHandlerContext ctx, String data) {
        log.info("Received ping, responding with pong");
        tcpMessageHelper.sendResponse(ctx, "pong");
    }
    
    /**
     * æ¾¶å­æhelloå¨å ä¼
     */
    @TcpHandler("hello")
    public void handleHello(ChannelHandlerContext ctx, String data) {
        log.info("Received hello");
        tcpMessageHelper.sendResponse(ctx, "Hello from TCP Server!");
    }
    
    /**
     * æ¾¶å­æé§è¯²ç¶å¨å ä¼éå å¨é¢ã¦îéæ¬å°®é°å¶ç´
     */
    @TcpHandler("^login:.*")
    public void handleLogin(ChannelHandlerContext ctx, String data) {
        String username = data.substring(6); // éç»å¸"login:"éå¶ç´
        log.info("User {} is logging in", username);
        tcpMessageHelper.sendResponse(ctx, "Login successful: " + username);
    }
    
    /**
     * æ¦æ¨¿î»æ¾¶å­æé£
     */
    @TcpHandler
    public void handleDefault(ChannelHandlerContext ctx, String data) {
        log.debug("Received default message: {}", data);
        tcpMessageHelper.sendResponse(ctx, "Echo: " + data);
    }
}

