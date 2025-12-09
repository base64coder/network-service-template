package com.dtc.core.bootstrap.ioc.lazysingleton;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;

/**
 * 延迟单例作用域
 * 注册延迟单例作用域
 * 
 * @author Network Service Template
 */
public class LazySingletonScope extends AbstractModule {

    private static final Scope LAZY_SINGLETON_SCOPE = new LazySingletonScopeImpl();

    @Override
    protected void configure() {
        bindScope(LazySingleton.class, LAZY_SINGLETON_SCOPE);
    }
}
