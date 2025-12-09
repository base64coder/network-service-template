package com.dtc.udp.handler;

import com.dtc.core.network.udp.UdpMessageHelper;
import com.dtc.annotations.web.MessageHandler;
import com.dtc.annotations.web.UdpHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;

/**
 * UDP忙露聢忙聛炉氓陇聞莽聬聠氓聶篓莽陇潞盲戮?
 * 忙录聰莽陇潞氓娄聜盲陆聲盲陆驴莽聰篓忙鲁篓猫搂拢茅漏卤氓聤篓莽職聞忙聳鹿氓录聫氓陇聞莽聬聠UDP忙露聢忙聛炉
 * 
 * @author Network Service Template
 */
@MessageHandler(protocol = "UDP")
@Singleton
public class ExampleUdpHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ExampleUdpHandler.class);
    
    private final UdpMessageHelper udpMessageHelper;
    
    @Inject
    public ExampleUdpHandler(UdpMessageHelper udpMessageHelper) {
        this.udpMessageHelper = udpMessageHelper;
    }
    
    /**
     * 氓陇聞莽聬聠ping忙露聢忙聛炉
     */
    @UdpHandler("ping")
    public void handlePing(ChannelHandlerContext ctx, 
                          InetSocketAddress sender, 
                          String data, 
                          byte[] binaryData) {
        log.info("Received ping from {}, responding with pong", sender);
        udpMessageHelper.sendResponse(ctx, sender, "pong");
    }
    
    /**
     * 氓陇聞莽聬聠hello忙露聢忙聛炉
     */
    @UdpHandler("hello")
    public void handleHello(ChannelHandlerContext ctx, 
                            InetSocketAddress sender, 
                            String data, 
                            byte[] binaryData) {
        log.info("Received hello from {}", sender);
        udpMessageHelper.sendResponse(ctx, sender, "Hello from UDP Server!");
    }
    
    /**
     * 氓陇聞莽聬聠忙聣聙忙聹聣盲禄楼"cmd:"氓录聙氓陇麓莽職聞氓聭陆盲禄陇忙露聢忙聛炉
     */
    @UdpHandler("cmd:*")
    public void handleCommand(ChannelHandlerContext ctx, 
                              InetSocketAddress sender, 
                              String data, 
                              byte[] binaryData) {
        String command = data.substring(4); // 氓聨禄忙聨聣"cmd:"氓聣聧莽录聙
        log.info("Received command from {}: {}", sender, command);
        
        // 氓陇聞莽聬聠氓聭陆盲禄陇茅聙禄猫戮聭
        String response = "Command executed: " + command;
        udpMessageHelper.sendResponse(ctx, sender, response);
    }
    
    /**
     * 茅禄聵猫庐陇氓陇聞莽聬聠氓聶篓茂录聢氓陇聞莽聬聠忙聣聙忙聹聣氓聟露盲禄聳忙露聢忙聛炉茂录聣
     */
    @UdpHandler
    public void handleDefault(ChannelHandlerContext ctx, 
                             InetSocketAddress sender, 
                             String data, 
                             byte[] binaryData) {
        log.debug("Received default message from {}: {}", sender, data);
        udpMessageHelper.sendResponse(ctx, sender, "Echo: " + data);
    }
}

