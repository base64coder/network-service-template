package com.dtc.core.bootstrap.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.ioc.core.NetModule;
import com.dtc.ioc.core.NetApplicationContext;
import com.dtc.ioc.core.context.AnnotationConfigApplicationContext;

/**
 * IoC 容器工厂
 * 负责创建和配置依赖注入容器
 * 
 * @author Network Service Template
 */
public class IoCContainerFactory {

    private static final Logger log = LoggerFactory.getLogger(IoCContainerFactory.class);

    /**
     * 创建网络服务主容器
     *
     * @param configuration 服务器配置
     * @return 注入器 (ApplicationContext)
     */
    @Nullable
    public static NetApplicationContext bootstrapInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Network Service IoC container...");

        try {
            // 使用 AnnotationConfigApplicationContext 作为容器实现
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            
            // 注册核心模块
            registerModule(context, new SystemInformationModule(configuration));
            registerModule(context, new LazySingletonModule());
            registerModule(context, new LifecycleModule());
            registerModule(context, new ConfigurationModule(configuration));
            registerModule(context, new PersistenceModule());
            registerModule(context, new NettyModule());
            registerModule(context, new NetworkServiceMainModule());
            registerModule(context, new ExtensionModule());
            registerModule(context, new ExtensionDependencyModule());
            registerModule(context, new MetricsModule());
            registerModule(context, new SecurityModule());
            registerModule(context, new DiagnosticModule());
            registerModule(context, new SerializationModule());
            registerModule(context, new QueueModule());
            registerModule(context, new CodecModule());
            registerModule(context, new ValidationModule());

            // 动态加载外部模块
            java.util.ServiceLoader.load(ModuleProvider.class).forEach(provider -> {
                log.info("Loading modules from provider: {}", provider.getClass().getName());
                provider.getModules().forEach(module -> {
                     if (module instanceof NetModule) {
                         registerModule(context, (NetModule) module);
                     }
                });
            });

            context.refresh();
            return context;

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
    public static NetApplicationContext extensionInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Extension System IoC container...");
        
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        registerModule(context, new SystemInformationModule(configuration));
        registerModule(context, new ConfigurationModule(configuration));
        registerModule(context, new LazySingletonModule());
        registerModule(context, new LifecycleModule());
        registerModule(context, new ExtensionModule());
        
        context.refresh();
        return context;
    }

    /**
     * 创建持久化系统容器
     */
    @NotNull
    public static NetApplicationContext persistenceInjector(@NotNull ServerConfiguration configuration) {
        log.info("Bootstrapping Persistence System IoC container...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        registerModule(context, new SystemInformationModule(configuration));
        registerModule(context, new ConfigurationModule(configuration));
        registerModule(context, new LazySingletonModule());
        registerModule(context, new LifecycleModule());
        registerModule(context, new PersistenceModule());
        
        context.refresh();
        return context;
    }
    
    private static void registerModule(NetApplicationContext context, NetModule module) {
        module.configure(context);
    }
}
