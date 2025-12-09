package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;

/**
     * ç½ç»åºç¨ä¸ä¸ææ¥å£
åé´Spring ApplicationContextåGuice Injectorçä¼ç¹
@author Network Service Template
/
public interface NetworkApplicationContext {
    
    /**
     * è·åBeanå®ä¾
@param beanType Beanç±»å
@return Beanå®ä¾
/
    @Nullable
    <T> T getBean(Class<T> beanType);
    
    /**
     * æ ¹æ®åç§°è·åBeanå®ä¾
@param beanName Beanåç§°
@return Beanå®ä¾
/
    @Nullable
    Object getBean(String beanName);
    
    /**
     * æ ¹æ®åç§°åç±»åè·åBeanå®ä¾
@param beanName Beanåç§°
@param beanType Beanç±»å
@return Beanå®ä¾
/
    @Nullable
    <T> T getBean(String beanName, Class<T> beanType);
    
    /**
     * è·åæå®ç±»åçææBeanå®ä¾
@param beanType Beanç±»å
@return Beanå®ä¾æ å°
/
    @NotNull
    <T> Map<String, T> getBeansOfType(Class<T> beanType);
    
    /**
     * æ£æ¥Beanæ¯å¦å­å¨
@param beanName Beanåç§°
@return æ¯å¦å­å¨
/
    boolean containsBean(String beanName);
    
    /**
     * æ£æ¥Beanæ¯å¦ä¸ºåä¾
@param beanName Beanåç§°
@return æ¯å¦ä¸ºåä¾
/
    boolean isSingleton(String beanName);
    
    /**
     * è·åBeançç±»å
@param beanName Beanåç§°
@return Beanç±»å
/
    @Nullable
    Class<?> getType(String beanName);
    
    /**
     * è·åææBeanåç§°
@return Beanåç§°æ°ç»
/
    @NotNull
    String[] getBeanDefinitionNames();
    
    /**
     * å·æ°å®¹å¨
/
    void refresh();
    
    /**
     * å³é­å®¹å¨
/
    void close();
    
    /**
     * æ£æ¥å®¹å¨æ¯å¦æ´»è·
@return æ¯å¦æ´»è·
/
    boolean isActive();
    
    /**
     * æ³¨åBeanå®ä¹
@param beanName Beanåç§°
@param beanClass Beanç±»å
/
    void registerBean(String beanName, Class<?> beanClass);
    
    /**
     * æ³¨åBeanå®ä¾
@param beanName Beanåç§°
@param beanInstance Beanå®ä¾
/
    void registerBean(String beanName, Object beanInstance);
    
    /**
     * åå¸åºç¨äºä»¶
@param event åºç¨äºä»¶
/
    void publishEvent(ApplicationEvent event);
    
    /**
     * æ·»å åºç¨çå¬å¨
@param listener åºç¨çå¬å¨
/
    void addApplicationListener(ApplicationListener<?> listener);
    
    /**
     * æ·»å Beanåå¤çå¨
@param beanPostProcessor Beanåå¤çå¨
/
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    
    /**
     * æ·»å Beanå·¥ååå¤çå¨
@param beanFactoryPostProcessor Beanå·¥ååå¤çå¨
/
    void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);
}