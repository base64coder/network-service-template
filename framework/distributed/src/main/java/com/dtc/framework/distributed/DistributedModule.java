package com.dtc.framework.distributed;

import com.dtc.core.bootstrap.launcher.StartupHook;
import com.dtc.core.cluster.registry.RegistryFactory;
import com.dtc.framework.distributed.manager.ClusterManager;
import com.dtc.framework.distributed.raft.registry.RaftRegistryFactory;
import com.dtc.framework.distributed.rpc.RpcProviderRegistry;
import com.dtc.framework.distributed.rpc.RpcServiceBeanPostProcessor;
import com.dtc.framework.beans.BeanPostProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class DistributedModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(RegistryFactory.class).to(RaftRegistryFactory.class).in(Singleton.class);
        bind(RpcProviderRegistry.class).in(Singleton.class);
        bind(ClusterManager.class).in(Singleton.class);
        bind(com.dtc.framework.distributed.rpc.RpcServer.class).in(Singleton.class);
        bind(com.dtc.framework.distributed.rpc.RpcClient.class).in(Singleton.class);
        
        // Register BeanPostProcessor
        Multibinder<BeanPostProcessor> binder = Multibinder.newSetBinder(binder(), BeanPostProcessor.class);
        binder.addBinding().to(RpcServiceBeanPostProcessor.class);
        binder.addBinding().to(com.dtc.framework.distributed.rpc.RpcReferenceBeanPostProcessor.class);
        
        // Register StartupHook
        Multibinder<StartupHook> hookBinder = Multibinder.newSetBinder(binder(), StartupHook.class);
        hookBinder.addBinding().to(ClusterManager.class);
    }
}
