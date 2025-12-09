package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;

/**
     * IoCæ¨¡åæ¥å£
æä¾æ¨¡ååéç½®åè½ï¼ç±»ä¼¼GuiceçModule
@author Network Service Template
/
public interface IoCModule {
    
    /**
     * éç½®æ¨¡å
å¨æ­¤æ¹æ³ä¸­æ³¨åBeanå®ä¹åéç½®
@param context åºç¨ä¸ä¸æ
/
    void configure(@NotNull NetworkApplicationContext context);
    
    /**
     * è·åæ¨¡ååç§°
@return æ¨¡ååç§°
/
    @NotNull
    String getModuleName();
    
    /**
     * è·åæ¨¡åçæ¬
@return æ¨¡åçæ¬
/
    @NotNull
    String getModuleVersion();
    
    /**
     * è·åæ¨¡åæè¿°
@return æ¨¡åæè¿°
/
    @NotNull
    String getModuleDescription();
    
    /**
     * è·åä¾èµçæ¨¡å
@return ä¾èµæ¨¡ååç§°åè¡¨
/
    @NotNull
    String[] getDependencies();
}
