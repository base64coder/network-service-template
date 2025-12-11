package com.dtc.framework.aop.framework;

public interface AopProxyFactory {
    AopProxy createAopProxy(AdvisedSupport config) throws Exception;
}

