package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.IoCModule;
import com.dtc.ioc.core.NetworkApplicationContext;

/**
     * IoCæ¨¡åæ½è±¡åºç±»
æä¾æ¨¡ååéç½®çåºç¡å®ç°
@author Network Service Template
/
public abstract class AbstractIoCModule implements IoCModule {
    
    @Override
    @NotNull
    public String getModuleName() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    @NotNull
    public String getModuleVersion() {
        return "1.0.0";
    }
    
    @Override
    @NotNull
    public String getModuleDescription() {
        return "IoC Module: " + getModuleName();
    }
    
    @Override
    @NotNull
    public String[] getDependencies() {
        return new String[0];
    }
    
    /**
     * ç»å®æ¥å£å°å®ç°ç±»
@param context åºç¨ä¸ä¸æ
@param interfaceClass æ¥å£ç±»
@param implementationClass å®ç°ç±»
/
    protected <T> void bind(NetworkApplicationContext context, 
                           Class<T> interfaceClass, 
                           Class<? extends T> implementationClass) {
        context.registerBean(interfaceClass.getSimpleName(), implementationClass);
    }
    
    /**
     * ç»å®æ¥å£å°å®ç°ç±»ï¼æå®åç§°ï¼
@param context åºç¨ä¸ä¸æ
@param name Beanåç§°
@param implementationClass å®ç°ç±»
/
    protected void bind(NetworkApplicationContext context, 
                       String name, 
                       Class<?> implementationClass) {
        context.registerBean(name, implementationClass);
    }
    
    /**
     * ç»å®åä¾å®ä¾
@param context åºç¨ä¸ä¸æ
@param name Beanåç§°
@param instance å®ä¾
/
    protected void bindInstance(NetworkApplicationContext context, 
                               String name, 
                               Object instance) {
        context.registerBean(name, instance);
    }
    
    /**
     * ç»å®æ¥å£å°åä¾å®ä¾
@param context åºç¨ä¸ä¸æ
@param interfaceClass æ¥å£ç±»
@param instance å®ä¾
/
    protected <T> void bindInstance(NetworkApplicationContext context, 
                                   Class<T> interfaceClass, 
                                   T instance) {
        context.registerBean(interfaceClass.getSimpleName(), instance);
    }
}
