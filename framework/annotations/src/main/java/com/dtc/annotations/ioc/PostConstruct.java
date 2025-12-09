package com.dtc.annotations.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * åæé æ³¨è§£
æ è¯Beanåå§ååè°ç¨çæ¹æ³
åé´Springç@PostConstructæ³¨è§£
@author Network Service Template
/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}

