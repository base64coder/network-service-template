package com.dtc.annotations.persistence;

/**
     * çº§èæä½ç±»åæä¸¾
åé´JPAçCascadeType
@author Network Service Template
/
public enum CascadeType {
    
    /**
     * ææçº§èæä½
/
    ALL,
    
    /**
     * æä¹åï¼ä¿å­ï¼æ¶çº§è
/
    PERSIST,
    
    /**
     * åå¹¶ï¼æ´æ°ï¼æ¶çº§è
/
    MERGE,
    
    /**
     * å é¤æ¶çº§è
/
    REMOVE,
    
    /**
     * å·æ°æ¶çº§è
/
    REFRESH,
    
    /**
     * åç¦»æ¶çº§è
/
    DETACH
}

