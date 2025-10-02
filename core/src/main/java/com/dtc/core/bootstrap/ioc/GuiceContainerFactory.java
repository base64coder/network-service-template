package com.dtc.core.bootstrap.ioc;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionLoader;
import com.dtc.core.extensions.ExtensionLifecycleHandler;
import com.dtc.core.extensions.ExtensionManager;
import com.dtc.core.netty.NettyBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络服务Guice启动器
 * 负责创建和配置依赖注入容器
 * 
 * @author Network Service Template
 */
public class GuiceContainerFactory {

    private static final Logger log = LoggerFactory.getLogger(GuiceContainerFactory.class);

    /**
     * 创建主要的依赖注入容器
     *
     * @param configuration 服务器配置
     * @return 依赖注入容器
     */
    @Nullable
    public static Injector bootstrapInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Network Service Guice container...");

        try {
            final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

            // 基础模块
            modules.add(
                    /* 系统信息模块 */
                    new SystemInformationModule(configuration),
                    /* 懒加载单例模块 */
                    new LazySingletonModule(),
                    /* 生命周期模块 */
                    new LifecycleModule(),
                    /* 配置模块 */
                    new ConfigurationModule(configuration),
                    /* Netty网络模块 */
                    new NettyModule(),
                    /* 网络服务主模块 */
                    new NetworkServiceMainModule(),
                    /* 扩展系统模块 */
                    new ExtensionModule(),
                    /* 指标监控模块 */
                    new MetricsModule(),
                    /* 安全模块 */
                    new SecurityModule(),
                    /* 诊断模块 */
                    new DiagnosticModule());

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
     * 创建扩展系统的依赖注入容器
     *
     * @param configuration 服务器配置
     * @return 扩展系统注入容器
     */
    @NotNull
    public static Injector extensionInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Extension System Guice container...");

        final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

        modules.add(
                new SystemInformationModule(configuration),
                new ConfigurationModule(configuration),
                new LazySingletonModule(),
                new LifecycleModule(),
                new ExtensionModule());

        return Guice.createInjector(Stage.PRODUCTION, modules.build());
    }

    /**
     * 创建持久化系统的依赖注入容器
     *
     * @param configuration 服务器配置
     * @return 持久化系统注入容器
     */
    @NotNull
    public static Injector persistenceInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Persistence System Guice container...");

        final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();

        modules.add(
                new SystemInformationModule(configuration),
                new ConfigurationModule(configuration),
                new LazySingletonModule(),
                new LifecycleModule(),
                new PersistenceModule());

        return Guice.createInjector(Stage.PRODUCTION, modules.build());
    }
}
