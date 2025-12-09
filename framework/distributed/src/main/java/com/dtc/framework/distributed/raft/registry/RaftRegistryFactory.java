package com.dtc.framework.distributed.raft.registry;

import com.dtc.core.cluster.registry.*;

public class RaftRegistryFactory implements RegistryFactory {
    
    @Override
    public ServiceRegistry createRegistry(RegistryConfig config) {
        return new RaftServiceRegistry(config);
    }

    @Override
    public ServiceDiscovery createDiscovery(RegistryConfig config) {
        // RaftServiceRegistry implements both interfaces
        return new RaftServiceRegistry(config);
    }

    @Override
    public String supportType() {
        return "raft";
    }
}

