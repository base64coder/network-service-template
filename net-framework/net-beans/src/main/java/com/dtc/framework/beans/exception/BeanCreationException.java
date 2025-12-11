package com.dtc.framework.beans.exception;

public class BeanCreationException extends BeansException {
    public BeanCreationException(String beanName, String msg) {
        super("Error creating bean with name '" + beanName + "': " + msg);
    }

    public BeanCreationException(String beanName, String msg, Throwable cause) {
        super("Error creating bean with name '" + beanName + "': " + msg, cause);
    }
}

