package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
     * Beanå®ä¹æ¥å£
æè¿°Beançåæ°æ®ä¿¡æ¯
åé´Spring BeanDefinitionçè®¾è®¡
@author Network Service Template
/
public interface BeanDefinition {
    
    /**
     * è·åBeanåç§°
@return Beanåç§°
/
    @NotNull
    String getBeanName();
    
    /**
     * è·åBeanç±»å
@return Beanç±»å
/
    @NotNull
    Class<?> getBeanClass();
    
    /**
     * è·åä½ç¨å
@return ä½ç¨å
/
    @NotNull
    BeanScope getScope();
    
    /**
     * æ¯å¦ä¸ºåä¾
@return æ¯å¦ä¸ºåä¾
/
    boolean isSingleton();
    
    /**
     * æ¯å¦ä¸ºåå
@return æ¯å¦ä¸ºåå
/
    boolean isPrototype();
    
    /**
     * æ¯å¦ä¸ºæå è½½
@return æ¯å¦ä¸ºæå è½½
/
    boolean isLazyInit();
    
    /**
     * è·åä¾èµçBeanåç§°åè¡¨
@return ä¾èµåè¡¨
/
    @NotNull
    List<String> getDependsOn();
    
    /**
     * è·ååå§åæ¹æ³åç§°
@return åå§åæ¹æ³åç§°
/
    @Nullable
    String getInitMethodName();
    
    /**
     * è·åéæ¯æ¹æ³åç§°
@return éæ¯æ¹æ³åç§°
/
    @Nullable
    String getDestroyMethodName();
    
    /**
     * è·åæé å½æ°
@return æé å½æ°
/
    @Nullable
    Constructor<?> getConstructor();
    
    /**
     * è·åå·¥åæ¹æ³
@return å·¥åæ¹æ³
/
    @Nullable
    Method getFactoryMethod();
    
    /**
     * è·åå±æ§å¼
@return å±æ§å¼æ å°
/
    @NotNull
    Map<String, Object> getPropertyValues();
    
    /**
     * è·åæ³¨è§£åæ°æ®
@return æ³¨è§£åæ°æ®æ å°
/
    @NotNull
    Map<String, Object> getAnnotationMetadata();
}
