package com.dtc.framework.distributed.manager;

import com.dtc.core.bootstrap.launcher.StartupHook;
import com.dtc.core.cluster.registry.RegistryConfig;
import com.dtc.core.cluster.registry.RegistryFactory;
import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.core.cluster.registry.ServiceRegistry;
import com.dtc.framework.distributed.rpc.RpcProviderRegistry;
import com.dtc.api.rpc.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 集群管理器
 * 负责启动注册中心，并注册本地服务
 */
@Singleton
public class ClusterManager implements StartupHook {
    
    private static final Logger log = LoggerFactory.getLogger(ClusterManager.class);
    
    private final RegistryFactory registryFactory;
    private final RpcProviderRegistry providerRegistry;
    private ServiceRegistry serviceRegistry;
    private boolean isStarted = false;
    
    @Inject
    public ClusterManager(RegistryFactory registryFactory, RpcProviderRegistry providerRegistry) {
        this.registryFactory = registryFactory;
        this.providerRegistry = providerRegistry;
    }
    
    public void start(RegistryConfig config) {
        if (isStarted) {
            return;
        }
        
        log.info("Starting ClusterManager with type: {}", config.getType());
        
        try {
            // 1. 启动注册中心客户端
            this.serviceRegistry = registryFactory.createRegistry(config);
            
            // 2. 注册已扫描到的 RPC 服务
            registerLocalServices(config);
            
            isStarted = true;
        } catch (Exception e) {
            log.error("Failed to start ClusterManager", e);
        }
    }
    
    public void stop() {
        if (!isStarted) {
            return;
        }
        // TODO: 注销服务，关闭 Registry
        isStarted = false;
    }
    
    @Override
    public void onServerStartup() {
        // Read config from System properties
        RegistryConfig config = new RegistryConfig();
        config.setType(System.getProperty("cluster.registry.type", "raft"));
        config.setAddress(System.getProperty("cluster.registry.address", "127.0.0.1:8888"));
        config.setGroup(System.getProperty("cluster.registry.group", "default_group"));
        config.addProperty("dataPath", System.getProperty("cluster.data.path", "raft_data"));
        
        start(config);
    }

    @Override
    public void onServerShutdown() {
        stop();
    }
    
    private void registerLocalServices(RegistryConfig config) {
        String host = getLocalHost();
        int port = 8080; // TODO: Get actual port
        
        Map<String, Object> beans = providerRegistry.getServiceBeans();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String serviceName = entry.getKey();
            RpcService metadata = providerRegistry.getMetadata(serviceName);
            
            ServiceInstance instance = new ServiceInstance();
            instance.setServiceId(UUID.randomUUID().toString());
            instance.setServiceName(serviceName);
            instance.setHost(host);
            instance.setPort(port);
            
            Map<String, String> meta = new HashMap<>();
            meta.put("version", metadata.version());
            meta.put("group", metadata.group());
            meta.put("weight", String.valueOf(metadata.weight()));
            instance.setMetadata(meta);
            
            serviceRegistry.register(instance).whenComplete((v, e) -> {
                if (e != null) {
                    log.error("Failed to register service: " + serviceName, e);
                } else {
                    log.info("Service registered: {}", serviceName);
                }
            });
        }
    }
    
    private String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
