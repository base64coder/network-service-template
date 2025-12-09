package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.util.Optional;

/ææææ
ææ æ©å±åæ­¢è¾åºåæ°
ææ 
ææ @author Network Service Template
ææ/
public interface ExtensionStopOutput {

    /ææææ
ææ è®¾ç½®åæ­¢å¤±è´¥åå 
ææ 
ææ @param reason å¤±è´¥åå 
ææ/
    void preventStop(@NotNull String reason);

    /ææææ
ææ è·ååæ­¢å¤±è´¥åå 
ææ 
ææ @return å¤±è´¥åå ï¼å¦æåæ­¢æååè¿åç©º
ææ/
    @NotNull
    Optional<String> getReason();

    /ææææ
ææ è®¾ç½®æ¸çå»¶è¿æ¶é´ï¼æ¯«ç§ï¼
ææ 
ææ @param delayMs å»¶è¿æ¶é´
ææ/
    void setCleanupDelay(long delayMs);

    /ææææ
ææ è·åæ¸çå»¶è¿æ¶é´
ææ 
ææ @return å»¶è¿æ¶é´
ææ/
    long getCleanupDelay();
}
