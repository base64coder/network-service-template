package com.dtc.core.network.mqtt;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * MQTT éå¶å§é£ã¥çé? * çç»çç» ï¼æ MQTT éå¿îæ©ç´å¸´éå±¾ç§·é­îî©é? * 
 * @author Network Service Template
 */
@Singleton
public class MqttServer {

    private static final Logger log = LoggerFactory.getLogger(MqttServer.class);

    // å¨ã¦å°éæ­QTTéå¶å§é£ã¥æéã§æ±NettyServerç¼ç¶ç«´ç» ï¼æ
    // private volatile boolean started = false; // å®¸æç°¾å¯®?    private int port = 1883;
    private String host = "0.0.0.0";

    public MqttServer() {
        log.info("Creating MQTT Server instance");
    }

    // å¨ã¦å°éæ­QTTéå¶å§é£ã¥æé?éæ»îé¢ç¢ettyServerç¼ç¶ç«´ç» ï¼æ
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
}
