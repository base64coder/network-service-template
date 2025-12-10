package com.dtc.framework.distributed.raft.registry;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.dtc.core.cluster.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Raft的服务注册实现
 */
public class RaftServiceRegistry implements ServiceRegistry, ServiceDiscovery {
    
    private static final Logger log = LoggerFactory.getLogger(RaftServiceRegistry.class);
    
    private final String groupId;
    private final String dataPath;
    private final String serverIdStr;
    private final String initConfStr;
    
    private Node node;
    private RegistryStateMachine fsm;
    private RaftGroupService raftGroupService;
    
    public RaftServiceRegistry(RegistryConfig config) {
        this.groupId = config.getGroup() != null ? config.getGroup() : "registry_group";
        this.dataPath = config.getProperties().getOrDefault("dataPath", "raft_data/registry");
        // Assume format "ip:port"
        this.serverIdStr = config.getAddress(); 
        // Initial cluster configuration
        this.initConfStr = config.getProperties().getOrDefault("initConf", this.serverIdStr);
        
        init();
    }
    
    private void init() {
        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setLogUri(dataPath + "/log");
        nodeOptions.setRaftMetaUri(dataPath + "/raft_meta");
        nodeOptions.setSnapshotUri(dataPath + "/snapshot");
        
        this.fsm = new RegistryStateMachine();
        nodeOptions.setFsm(this.fsm);
        
        // 30s snapshot interval
        nodeOptions.setSnapshotIntervalSecs(30);
        
        PeerId serverId = new PeerId();
        if (!serverId.parse(serverIdStr)) {
            throw new IllegalArgumentException("Fail to parse serverId: " + serverIdStr);
        }
        
        Configuration initConf = new Configuration();
        if (!initConf.parse(initConfStr)) {
            throw new IllegalArgumentException("Fail to parse initConf: " + initConfStr);
        }
        nodeOptions.setInitialConf(initConf);
        
        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions);
        this.node = this.raftGroupService.start();
        
        log.info("Raft Registry Node started on {}", serverIdStr);
    }

    @Override
    public CompletableFuture<Void> register(ServiceInstance instance) {
        RegistryOperation op = RegistryOperation.register(
            instance.getServiceName(),
            instance.getServiceId(),
            instance.getHost(),
            instance.getPort(),
            instance.getMetadata()
        );
        return applyOperation(op);
    }

    @Override
    public CompletableFuture<Void> deregister(ServiceInstance instance) {
        RegistryOperation op = RegistryOperation.deregister(
            instance.getServiceName(),
            instance.getServiceId()
        );
        return applyOperation(op);
    }

    @Override
    public String getType() {
        return "raft";
    }

    @Override
    public CompletableFuture<List<ServiceInstance>> getInstances(String serviceName) {
        // Linearizable read
        // In real impl, should use readIndex for strong consistency or local read for performance
        // Here we just read from FSM for simplicity
        return CompletableFuture.completedFuture(fsm.getInstances(serviceName));
    }

    @Override
    public CompletableFuture<List<String>> getServices() {
        return CompletableFuture.completedFuture(fsm.getServices());
    }

    @Override
    public void subscribe(String serviceName, ServiceListener listener) {
        fsm.subscribe(serviceName, listener);
    }

    @Override
    public void unsubscribe(String serviceName, ServiceListener listener) {
        fsm.unsubscribe(serviceName, listener);
    }

    private CompletableFuture<Void> applyOperation(RegistryOperation op) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (!isLeader()) {
            // Forward request to leader or fail
            // For simplicity, we fail here. Client should retry with leader.
            // A production impl would forward RPC.
            future.completeExceptionally(new IllegalStateException("Not leader"));
            return future;
        }

        try {
            final Task task = new Task();
            task.setData(ByteBuffer.wrap(serialize(op)));
            task.setDone(new Closure() {
                @Override
                public void run(Status status) {
                    if (status.isOk()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new RuntimeException(status.getErrorMsg()));
                    }
                }
            });
            this.node.apply(task);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private boolean isLeader() {
        return this.node.isLeader();
    }
    
    private byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }
}

