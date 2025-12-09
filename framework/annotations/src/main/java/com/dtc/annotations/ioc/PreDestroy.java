package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * é¢éæ¯æ³¨è§£
æ è¯Beanéæ¯åè°ç¨çæ¹æ³
åé´Springç@PreDestroyæ³¨è§£
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}

