package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

/ææææ
ææ æå¡å¨ä¿¡æ¯æ¥å£
ææ 
ææ @author Network Service Template
ææ/
public interface ServerInformation {

    /ææææ
ææ è·åæå¡å¨åç§°
ææ 
ææ @return æå¡å¨åç§°
ææ/
    @NotNull
    String getServerName();

    /ææææ
ææ è·åæå¡å¨çæ¬
ææ 
ææ @return æå¡å¨çæ¬
ææ/
    @NotNull
    String getServerVersion();

    /ææææ
ææ è·åæå¡å¨ID
ææ 
ææ @return æå¡å¨ID
ææ/
    @NotNull
    String getServerId();

    /ææææ
ææ è·åæ°æ®æä»¶å¤¹è·¯å¾
ææ 
ææ @return æ°æ®æä»¶å¤¹è·¯å¾
ææ/
    @NotNull
    Path getDataFolder();

    /ææææ
ææ è·åéç½®æä»¶å¤¹è·¯å¾
ææ 
ææ @return éç½®æä»¶å¤¹è·¯å¾
ææ/
    @NotNull
    Path getConfigFolder();

    /ææææ
ææ è·åæ©å±æä»¶å¤¹è·¯å¾
ææ 
ææ @return æ©å±æä»¶å¤¹è·¯å¾
ææ/
    @NotNull
    Path getExtensionsFolder();

    /ææææ
ææ è·åç³»ç»å±æ§
ææ 
ææ @return ç³»ç»å±æ§æ å°
ææ/
    @NotNull
    Map<String, String> getSystemProperties();

    /ææææ
ææ è·åç¯å¢åé
ææ 
ææ @return ç¯å¢åéæ å°
ææ/
    @NotNull
    Map<String, String> getEnvironmentVariables();

    /ææææ
ææ æ¯å¦è¿è¡å¨åµå¥å¼æ¨¡å¼
ææ 
ææ @return æ¯å¦åµå¥å¼æ¨¡å¼
ææ/
    boolean isEmbedded();
}
