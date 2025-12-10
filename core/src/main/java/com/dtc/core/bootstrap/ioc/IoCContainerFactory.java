package com.dtc.core.bootstrap.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * IoC 容器工厂
 * 负责创建和配置依赖注入容器
 * 暂时使用 Google Guice 实现
 * 
 * @author Network Service Template
 */
public class IoCContainerFactory {

    private static final Logger log = LoggerFactory.getLogger(IoCContainerFactory.class);

    /**
     * 创建网络服务主容器
     *
     * @param configuration 服务器配置
     * @return 注入器
     */
    @Nullable
    public static Injector bootstrapInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Network Service IoC container...");

        try {
            List<Module> modules = new ArrayList<>();
            
            // 注册核心模块
            modules.add(new SystemInformationModule(configuration));
            modules.add(new LazySingletonModule());
            modules.add(new LifecycleModule());
            modules.add(new ConfigurationModule(configuration));
            modules.add(new PersistenceModule());
            modules.add(new NettyModule());
            modules.add(new NetworkServiceMainModule());
            modules.add(new ExtensionModule());
            modules.add(new ExtensionDependencyModule());
            modules.add(new MetricsModule());
            modules.add(new SecurityModule());
            modules.add(new DiagnosticModule());
            modules.add(new SerializationModule());
            modules.add(new QueueModule());
            modules.add(new CodecModule());
            modules.add(new ValidationModule());

            // 动态加载外部模块
            java.util.ServiceLoader.load(ModuleProvider.class).forEach(provider -> {
                log.info("Loading modules from provider: {}", provider.getClass().getName());
                modules.addAll(provider.getModules());
            });

            Injector injector = Guice.createInjector(modules);
            log.info("Network Service IoC container bootstrapped successfully");
            return injector;

        } catch (Exception e) {
            log.error("Failed to bootstrap Network Service IoC container", e);
            if (log.isDebugEnabled()) {
                log.debug("Original Exception: ", e);
            }
            return null;
        }
    }
    
    /**
     * 创建扩展系统容器
     */
    @NotNull
    public static Injector extensionInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Extension System IoC container...");
        
        List<Module> modules = new ArrayList<>();
        modules.add(new SystemInformationModule(configuration));
        modules.add(new ConfigurationModule(configuration));
        modules.add(new LazySingletonModule());
        modules.add(new LifecycleModule());
        modules.add(new ExtensionModule());
        
        Injector injector = Guice.createInjector(modules);
        log.info("Extension System IoC container bootstrapped successfully");
        return injector;
    }

    /**
     * 创建持久化系统容器
     */
    @NotNull
    public static Injector persistenceInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Persistence System IoC container...");

        List<Module> modules = new ArrayList<>();
        modules.add(new SystemInformationModule(configuration));
        modules.add(new ConfigurationModule(configuration));
        modules.add(new LazySingletonModule());
        modules.add(new LifecycleModule());
        modules.add(new PersistenceModule());
        
        Injector injector = Guice.createInjector(modules);
        log.info("Persistence System IoC container bootstrapped successfully");
        return injector;
    }
}
