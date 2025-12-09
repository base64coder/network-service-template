package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * Beanå·¥åæ¥å£
ç®¡çBeançåå»ºåéç½®
åé´Spring BeanFactoryçè®¾è®¡
@author Network Service Template
/
public interface BeanFactory {
    
    /**
     * è·åBeanå®ä¾
@param name Beanåç§°
@return Beanå®ä¾
/
    @Nullable
    Object getBean(String name);
    
    /**
     * è·åBeanå®ä¾ï¼æå®ç±»åï¼
@param name Beanåç§°
@param requiredType å¿éç±»å
@return Beanå®ä¾
/
    @Nullable
    <T> T getBean(String name, Class<T> requiredType);
    
    /**
     * è·åBeanå®ä¾ï¼æå®ç±»åï¼
@param requiredType å¿éç±»å
@return Beanå®ä¾
/
    @Nullable
    <T> T getBean(Class<T> requiredType);
    
    /**
     * æ£æ¥Beanæ¯å¦å­å¨
@param name Beanåç§°
@return æ¯å¦å­å¨
/
    boolean containsBean(String name);
    
    /**
     * æ£æ¥Beanæ¯å¦ä¸ºåä¾
@param name Beanåç§°
@return æ¯å¦ä¸ºåä¾
/
    boolean isSingleton(String name);
    
    /**
     * è·åBeanç±»å
@param name Beanåç§°
@return Beanç±»å
/
    @Nullable
    Class<?> getType(String name);
    
    /**
     * è·åBeanå«å
@param name Beanåç§°
@return å«åæ°ç»
/
    @NotNull
    String[] getAliases(String name);
    
    /**
     * é¢å®ä¾ååä¾Bean
/
    void preInstantiateSingletons();
    
    /**
     * éæ¯åä¾Bean
/
    void destroySingletons();
}
