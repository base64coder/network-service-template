package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Custom éå¶å§é£ã¥çé? * çç»çç» ï¼æé·îç¾æ¶å¤å´çî¿ç¹éºã¥æ°å¨å ä¼æ¾¶å­æ
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomServer {

    private static final Logger log = LoggerFactory.getLogger(CustomServer.class);

    // å¨ã¦å°éæ¬³ustoméå¶å§é£ã¥æéã§æ±NettyServerç¼ç¶ç«´ç» ï¼æ
    // private volatile boolean started = false; // å®¸æç°¾å¯®?    private int port = 9999;
    private String host = "0.0.0.0";
    private String protocolName = "CustomProtocol";

    public CustomServer() {
        log.info("Creating Custom Server instance");
    }

    // å¨ã¦å°éæ¬³ustoméå¶å§é£ã¥æé?éæ»îé¢ç¢ettyServerç¼ç¶ç«´ç» ï¼æ
    // start(), stop(), isStarted() éè§ç¡¶å®¸è¬Ð©é?
    /**
     * é¾å³°å½éå¶å§é£ã§î¬é
     */
    public int getPort() {
        return port;
    }

    /**
     * çå§çéå¶å§é£ã§î¬é
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * é¾å³°å½éå¶å§é£ã¤å¯é
     */
    @NotNull
    public String getHost() {
        return host;
    }

    /**
     * çå§çéå¶å§é£ã¤å¯é
     */
    public void setHost(@NotNull String host) {
        this.host = host;
    }

    /**
     * é¾å³°å½éå¿îéå¶Ð
     */
    @NotNull
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * çå§çéå¿îéå¶Ð
     */
    public void setProtocolName(@NotNull String protocolName) {
        this.protocolName = protocolName;
    }
}
