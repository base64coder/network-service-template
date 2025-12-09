package com.dtc.core.network.tcp;

import com.dtc.api.annotations.NotNull;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
     * TCP åè®®å¤çå¨æ½è±¡ç±»
å®ä¹ TCP åè®®çå¤çé»è¾
@author Network Service Template
/
public abstract class TcpProtocolHandler {

    private static final Logger log = LoggerFactory.getLogger(TcpProtocolHandler.class);

    public TcpProtocolHandler() {
        log.info("Creating TCP Protocol Handler instance");
    }

    /**
     * å¤ç TCP è¿æ¥
/
    public abstract void handleConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * å¤ç TCP æ­å¼è¿æ¥
/
    public abstract void handleDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * å¤ç TCP æ¶æ¯
/
    public abstract void handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message);

    /**
     * å¤ç TCP å¼å¸¸
/
    public abstract void handleException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause);

    /**
     * è·ååè®®åç§°
/
    @NotNull
    public abstract String getProtocolName();

    /**
     * è·ååè®®çæ¬
/
    @NotNull
    public abstract String getProtocolVersion();

    /**
     * æ£æ¥æ¯å¦æ¯æè¯¥æ¶æ¯ç±»å
/
    public abstract boolean supports(@NotNull Class<?> messageType);
}
