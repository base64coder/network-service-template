package com.dtc.framework.beans.module;

/**
 * 抽象模块基类，用于兼容 Guice 的 AbstractModule 用法
 */
public abstract class AbstractModule implements Module {
    
    private Binder binder;

    @Override
    public final void configure(Binder binder) {
        this.binder = binder;
        try {
            configure();
        } finally {
            this.binder = null;
        }
    }

    /**
     * 子类实现此方法进行配置
     */
    protected abstract void configure();

    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        if (binder == null) {
            throw new IllegalStateException("Binder is not active. bind() can only be used inside configure()");
        }
        return binder.bind(type);
    }
    
    protected void install(Module module) {
        if (binder == null) {
            throw new IllegalStateException("Binder is not active");
        }
        binder.install(module);
    }
}

