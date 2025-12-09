package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.List;
import java.util.Map;

/**
     * å¯éç½®Beanå·¥åæ¥å£
æä¾Beanå·¥åçéç½®åè½
åé´Spring ConfigurableBeanFactoryçè®¾è®¡
@author Network Service Template
/
public interface ConfigurableBeanFactory extends BeanFactory {
    
    /**
     * è®¾ç½®Beanç±»å è½½å¨
@param beanClassLoader Beanç±»å è½½å¨
/
    void setBeanClassLoader(ClassLoader beanClassLoader);
    
    /**
     * è®¾ç½®Beanè¡¨è¾¾å¼è§£æå¨
@param resolver è¡¨è¾¾å¼è§£æå¨
/
    void setBeanExpressionResolver(BeanExpressionResolver resolver);
    
    /**
     * æ·»å å±æ§ç¼è¾å¨æ³¨åå¨
@param registrar å±æ§ç¼è¾å¨æ³¨åå¨
/
    void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);
    
    /**
     * æ·»å Beanåå¤çå¨
@param beanPostProcessor Beanåå¤çå¨
/
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    
    /**
     * è·åBeanåå¤çå¨æ°é
@return åå¤çå¨æ°é
/
    int getBeanPostProcessorCount();
    
    /**
     * æ³¨åBeanå®ä¹
@param beanName Beanåç§°
@param beanDefinition Beanå®ä¹
/
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
    
    /**
     * è·åBeanå®ä¹
@param beanName Beanåç§°
@return Beanå®ä¹
/
    @Nullable
    BeanDefinition getBeanDefinition(String beanName);
    
    /**
     * è·åææBeanå®ä¹
@return Beanå®ä¹æ å°
/
    @NotNull
    Map<String, BeanDefinition> getBeanDefinitions();
    
    /**
     * è·åBeanå®ä¹åç§°
@return Beanå®ä¹åç§°æ°ç»
/
    @NotNull
    String[] getBeanDefinitionNames();
    
    /**
     * æ³¨ååä¾Bean
@param beanName Beanåç§°
@param singletonObject åä¾å¯¹è±¡
/
    void registerSingleton(String beanName, Object singletonObject);
    
    /**
     * è·ååä¾Bean
@param beanName Beanåç§°
@return åä¾Bean
/
    @Nullable
    Object getSingleton(String beanName);
    
    /**
     * æ·»å åä¾Bean
@param beanName Beanåç§°
@param singletonObject åä¾å¯¹è±¡
/
    void addSingleton(String beanName, Object singletonObject);
    
    /**
     * è·ååä¾äºæ¥é
@return äºæ¥éå¯¹è±¡
/
    @NotNull
    Object getSingletonMutex();
    
    /**
     * è·åBeanåå¤çå¨åè¡¨
@return Beanåå¤çå¨åè¡¨
/
    @NotNull
    List<BeanPostProcessor> getBeanPostProcessors();
    
    /**
     * è·åBeanç±»å è½½å¨
@return Beanç±»å è½½å¨
/
    @Nullable
    ClassLoader getBeanClassLoader();
    
    /**
     * è·åBeanè¡¨è¾¾å¼è§£æå¨
@return Beanè¡¨è¾¾å¼è§£æå¨
/
    @Nullable
    BeanExpressionResolver getBeanExpressionResolver();
    
    /**
     * è·åå±æ§ç¼è¾å¨æ³¨åè¡¨
@return å±æ§ç¼è¾å¨æ³¨åè¡¨
/
    @Nullable
    PropertyEditorRegistry getPropertyEditorRegistry();
    
    /**
     * è®¾ç½®å±æ§ç¼è¾å¨æ³¨åè¡¨
@param propertyEditorRegistry å±æ§ç¼è¾å¨æ³¨åè¡¨
/
    void setPropertyEditorRegistry(PropertyEditorRegistry propertyEditorRegistry);
    
    /**
     * éæ¯Bean
@param beanName Beanåç§°
@param beanInstance Beanå®ä¾
@param definition Beanå®ä¹
/
    void destroyBean(String beanName, Object beanInstance, BeanDefinition definition);
    
    /**
     * æ¸çBeanå®ä¹
/
    void clearBeanDefinitions();
}
