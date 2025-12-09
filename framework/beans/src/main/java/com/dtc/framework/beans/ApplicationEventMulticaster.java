package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;

/**
     * åºç¨äºä»¶å¤æ­å¨æ¥å£
å¹¿æ­åºç¨äºä»¶
åé´Spring ApplicationEventMulticasterçè®¾è®¡
@author Network Service Template
/
public interface ApplicationEventMulticaster {
    
    /**
     * æ·»å åºç¨çå¬å¨
@param listener åºç¨çå¬å¨
/
    void addApplicationListener(@NotNull ApplicationListener<?> listener);
    
    /**
     * ç§»é¤åºç¨çå¬å¨
@param listener åºç¨çå¬å¨
/
    void removeApplicationListener(@NotNull ApplicationListener<?> listener);
    
    /**
     * å¹¿æ­åºç¨äºä»¶
@param event åºç¨äºä»¶
/
    void multicastEvent(@NotNull ApplicationEvent event);
    
    /**
     * ç§»é¤ææçå¬å¨
/
    void removeAllListeners();
}
