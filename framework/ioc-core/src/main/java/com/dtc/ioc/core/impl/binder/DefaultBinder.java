package com.dtc.ioc.core.impl.binder;

import com.dtc.ioc.core.BeanDefinition;
import com.dtc.ioc.core.BeanScope;
import com.dtc.ioc.core.NetApplicationContext;
import com.dtc.ioc.core.binder.Binder;
import com.dtc.ioc.core.binder.LinkedBindingBuilder;
import com.dtc.ioc.core.binder.ScopedBindingBuilder;
import com.dtc.ioc.core.impl.DefaultBeanDefinition;
import com.dtc.ioc.core.provider.Provider;

/**
 * 默认绑定器实现
 * 
 * 功能说明：
 * 1. 提供类型绑定功能，用于在模块配置中建立接口与实现类之间的映射关系
 * 2. 支持多种绑定方式：
 *    - 绑定到实现类：bind(Interface.class).to(Implementation.class)
 *    - 绑定到实例：bind(Interface.class).toInstance(instance)
 *    - 绑定到Provider：bind(Interface.class).toProvider(provider)
 * 3. 支持作用域配置：
 *    - 单例作用域：in(BeanScope.SINGLETON)
 *    - 立即初始化单例：asEagerSingleton()
 * 4. 支持模块安装：install(module) 用于安装其他模块的配置
 * 
 * 使用场景：
 * 在 NetModule 的 configure 方法中，通过 Binder 配置依赖注入关系。
 * 例如：
 * <pre>
 * public void configure(NetApplicationContext context) {
 *     Binder binder = context.getBinder();
 *     binder.bind(Service.class).to(ServiceImpl.class).in(BeanScope.SINGLETON);
 *     binder.bind(Config.class).toInstance(configInstance);
 * }
 * </pre>
 * 
 * 注意：这是自定义IOC容器的实现，目前框架暂时使用 Google Guice，此实现待完善。
 * 
 * @author Network Service Template
 */
public class DefaultBinder implements Binder {

    private final NetApplicationContext context;

    /**
     * 创建绑定器实例
     * 
     * @param context 应用上下文，用于注册Bean定义
     */
    public DefaultBinder(NetApplicationContext context) {
        this.context = context;
    }

    /**
     * 开始绑定特定类型
     * 
     * @param type 要绑定的类型（通常是接口）
     * @return 绑定构建器，用于链式配置绑定关系
     */
    @Override
    public <T> LinkedBindingBuilder<T> bind(Class<T> type) {
        return new BindingBuilderImpl<>(type, context);
    }

    /**
     * 安装另一个模块
     * 用于模块化配置，允许一个模块包含另一个模块的配置
     * 
     * @param module 要安装的模块
     */
    @Override
    public void install(com.dtc.ioc.core.NetModule module) {
        module.configure(context);
    }

    /**
     * 绑定构建器实现
     * 提供链式API用于配置绑定关系
     * 
     * @param <T> 绑定类型
     */
    private static class BindingBuilderImpl<T> implements LinkedBindingBuilder<T> {
        private final Class<T> type;
        private final NetApplicationContext context;
        private final DefaultBeanDefinition definition;

        /**
         * 创建绑定构建器
         * 
         * @param type 要绑定的类型
         * @param context 应用上下文
         */
        public BindingBuilderImpl(Class<T> type, NetApplicationContext context) {
            this.type = type;
            this.context = context;
            String beanName = type.getSimpleName();
            this.definition = new DefaultBeanDefinition(beanName, type);
            // 默认使用单例作用域
            this.definition.setScope(BeanScope.SINGLETON);
        }

        /**
         * 绑定到实现类
         * 建立接口与实现类之间的映射关系
         * 
         * @param implementation 实现类
         * @return 作用域构建器，用于配置作用域
         */
        @Override
        public ScopedBindingBuilder to(Class<? extends T> implementation) {
            // 在实际实现中，需要更新 BeanDefinition 指向实现类
            // 但保持 Bean 名称/键与接口类型 'type' 关联
            // 目前简化实现，假设直接注册实现类
            // context.registerBean(implementation); 
            // TODO: 增强注册处理，实现接口到实现类的映射
            return this;
        }

        /**
         * 绑定到具体实例
         * 直接将已创建的实例注册到容器中
         * 
         * @param instance 要绑定的实例
         */
        @Override
        public void toInstance(T instance) {
            String beanName = type.getSimpleName();
            context.registerBean(beanName, instance);
        }

        /**
         * 绑定到Provider实例
         * 使用Provider模式，延迟创建Bean实例
         * 
         * @param provider Provider实例
         * @return 作用域构建器
         */
        @Override
        public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
            // TODO: 实现基于Provider的注册
            return this;
        }

        /**
         * 绑定到Provider类
         * 使用Provider类，容器会创建Provider实例并调用其get方法
         * 
         * @param providerType Provider类
         * @return 作用域构建器
         */
        @Override
        public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
            // TODO: 实现基于Provider类的注册
            return this;
        }

        /**
         * 设置Bean的作用域
         * 
         * @param scope 作用域（单例、原型等）
         */
        @Override
        public void in(BeanScope scope) {
            definition.setScope(scope);
        }

        /**
         * 设置为立即初始化的单例
         * 容器启动时立即创建实例，而不是延迟到首次使用时
         */
        @Override
        public void asEagerSingleton() {
            definition.setScope(BeanScope.SINGLETON);
            definition.setLazyInit(false);
        }
    }
}

