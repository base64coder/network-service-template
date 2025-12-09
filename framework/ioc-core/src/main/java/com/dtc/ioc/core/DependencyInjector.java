package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
     * ä¾èµæ³¨å¥å¨æ¥å£
åé´Guiceçä¾èµæ³¨å¥æºå¶
@author Network Service Template
/
public interface DependencyInjector {
    
    /**
     * æ³¨å¥ä¾èµ
@param bean Beanå®ä¾
@param definition Beanå®ä¹
/
    void injectDependencies(Object bean, BeanDefinition definition);
    
    /**
     * æ³¨å¥å­æ®µä¾èµ
@param bean Beanå®ä¾
@param beanClass Beanç±»å
/
    void injectFieldDependencies(Object bean, Class<?> beanClass);
    
    /**
     * æ³¨å¥æé å½æ°ä¾èµ
@param constructor æé å½æ°
@param args åæ°
@return Beanå®ä¾
/
    Object createBeanWithConstructor(Constructor<?> constructor, Object[] args);
    
    /**
     * æ³¨å¥æ¹æ³ä¾èµ
@param bean Beanå®ä¾
@param beanClass Beanç±»å
/
    void injectMethodDependencies(Object bean, Class<?> beanClass);
}
