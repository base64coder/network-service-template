package com.dtc.ioc.core;

/**
     * Beanä½ç¨åæä¸¾
åé´Springçä½ç¨åè®¾è®¡
@author Network Service Template
/
public enum BeanScope {
    
    /**
     * åä¾ä½ç¨å - å®¹å¨ä¸­åªæä¸ä¸ªå®ä¾
/
    SINGLETON("singleton"),
    
    /**
     * ååä½ç¨å - æ¯æ¬¡è·åé½åå»ºæ°å®ä¾
/
    PROTOTYPE("prototype"),
    
    /**
     * è¯·æ±ä½ç¨å - æ¯ä¸ªHTTPè¯·æ±ä¸ä¸ªå®ä¾
/
    REQUEST("request"),
    
    /**
     * ä¼è¯ä½ç¨å - æ¯ä¸ªç¨æ·ä¼è¯ä¸ä¸ªå®ä¾
/
    SESSION("session");
    
    private final String value;
    
    BeanScope(String value) {
        this.value = value;
    }
    
    /**
     * è·åä½ç¨åå¼
@return ä½ç¨åå¼
/
    public String getValue() {
        return value;
    }
    
    /**
     * æ ¹æ®å¼è·åä½ç¨å
@param value ä½ç¨åå¼
@return ä½ç¨å
/
    public static BeanScope fromValue(String value) {
        for (BeanScope scope : values()) {
            if (scope.value.equals(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown bean scope: " + value);
    }
}
