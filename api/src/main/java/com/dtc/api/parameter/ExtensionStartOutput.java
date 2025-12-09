package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.util.Optional;

/ææææ
ææ æ©å±å¯å¨è¾åºåæ°
ææ 
ææ @author Network Service Template
ææ/
public interface ExtensionStartOutput {

    /ææææ
ææ è®¾ç½®å¯å¨å¤±è´¥åå 
ææ 
ææ @param reason å¤±è´¥åå 
ææ/
    void preventStartup(@NotNull String reason);

    /ææææ
ææ è·åå¯å¨å¤±è´¥åå 
ææ 
ææ @return å¤±è´¥åå ï¼å¦æå¯å¨æååè¿åç©º
ææ/
    @NotNull
    Optional<String> getReason();

    /ææææ
ææ è®¾ç½®æ©å±éç½®
ææ 
ææ @param key   éç½®é®
ææ @param value éç½®å¼
ææ/
    void setConfiguration(@NotNull String key, @NotNull String value);

    /ææææ
ææ è·åæ©å±éç½®
ææ 
ææ @param key éç½®é®
ææ @return éç½®å¼
ææ/
    @Nullable
    String getConfiguration(@NotNull String key);
}
