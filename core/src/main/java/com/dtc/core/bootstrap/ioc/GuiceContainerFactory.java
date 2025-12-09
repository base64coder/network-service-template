package com.dtc.core.bootstrap.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Guice 容器工厂
 * 负责创建和配置 Guice 依赖注入容器
 * 
 * @author Network Service Template
 */
public class GuiceContainerFactory {

    private static final Logger log = LoggerFactory.getLogger(GuiceContainerFactory.class);

    /**
     * 创建网络服务主容器
     *
     * @param configuration 服务器配置
     * @return Guice 注入器
     */
    @Nullable
    public static Injector bootstrapInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Network Service Guice container...");

        try {
            final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

            // 核心模块
            modules.add(
                    /* 系统信息模块 */
                    new SystemInformationModule(configuration),
                    /* 延迟单例模块 */
                    new LazySingletonModule(),
                    /* 生命周期模块 */
                    new LifecycleModule(),
                    /* 配置模块 */
                    new ConfigurationModule(configuration),
                    /* 持久化模块 */
                    new PersistenceModule(),
                    /* Netty 服务器模块 */
                    new NettyModule(),
                    /* 网络服务主模块 */
                    new NetworkServiceMainModule(),
                    /* 扩展系统模块 */
                    new ExtensionModule(),
                    /* 扩展依赖模块 */
                    new ExtensionDependencyModule(),
                    /* 指标模块 */
                    new MetricsModule(),
                    /* 安全模块 */
                    new SecurityModule(),
                    /* 诊断模块 */
                    new DiagnosticModule(),
                    /* 序列化模块 */
                    new SerializationModule(),
                    /* 队列模块 */
                    new QueueModule(),
                    /* 编解码器模块 */
                    new CodecModule(),
                    /* 验证模块 */
                    new ValidationModule());

            // 动态加载外部模块
            java.util.ServiceLoader.load(ModuleProvider.class).forEach(provider -> {
                log.info("Loading modules from provider: {}", provider.getClass().getName());
                modules.addAll(provider.getModules());
            });

            return Guice.createInjector(Stage.PRODUCTION, modules.build());

        } catch (Exception e) {
            log.error("Failed to bootstrap Network Service Guice container", e);
            if (log.isDebugEnabled()) {
                log.debug("Original Exception: ", e);
            }
            return null;
        }
    }

    /**
     * 创建扩展系统容器
     *
     * @param configuration 服务器配置
     * @return 扩展系统注入器
     */
    @NotNull
    public static Injector extensionInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Extension System Guice container...");

        final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

        modules.add(new SystemInformationModule(configuration), new ConfigurationModule(configuration),
                new LazySingletonModule(), new LifecycleModule(), new ExtensionModule());

        return Guice.createInjector(Stage.PRODUCTION, modules.build());
    }

    /**
     * 创建持久化系统容器
     *
     * @param configuration 服务器配置
     * @return 持久化系统注入器
     */
    @NotNull
    public static Injector persistenceInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Persistence System Guice container...");

        final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

        modules.add(new SystemInformationModule(configuration), new ConfigurationModule(configuration),
                new LazySingletonModule(), new LifecycleModule(), new PersistenceModule());

        return Guice.createInjector(Stage.PRODUCTION, modules.build());
    }
}
