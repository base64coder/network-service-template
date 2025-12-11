package com.dtc.net.cluster.raft.registry;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.dtc.core.cluster.registry.ServiceInstance;
import com.dtc.core.cluster.registry.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 注册中心状态机
 * 负责维护服务列表状态
 */
public class RegistryStateMachine extends StateMachineAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(RegistryStateMachine.class);
    
    /**
     * ServiceName -> List<ServiceInstance>
     */
    private final Map<String, List<ServiceInstance>> registryStore = new ConcurrentHashMap<>();
    
    /**
     * ServiceName -> List<ServiceListener>
     */
    private final Map<String, List<ServiceListener>> listeners = new ConcurrentHashMap<>();
    
    private final AtomicLong leaderTerm = new AtomicLong(-1);

    @Override
    public void onApply(Iterator iter) {
        while (iter.hasNext()) {
            ByteBuffer data = iter.getData();
            Closure done = iter.done();
            
            try {
                RegistryOperation op = deserialize(data);
                if (op != null) {
                    switch (op.getType()) {
                        case REGISTER:
                            handleRegister(op);
                            break;
                        case DEREGISTER:
                            handleDeregister(op);
                            break;
                    }
                }
                
                if (done != null) {
                    done.run(Status.OK());
                }
            } catch (Exception e) {
                log.error("Fail to apply registry operation", e);
                if (done != null) {
                    done.run(new Status(RaftError.EINTERNAL, "Fail to apply operation: %s", e.getMessage()));
                }
            }
            
            iter.next();
        }
    }

    private void handleRegister(RegistryOperation op) {
        String serviceName = op.getServiceName();
        List<ServiceInstance> instances = registryStore.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>());
        
        // Remove existing if present (update)
        instances.removeIf(i -> i.getServiceId().equals(op.getServiceId()));
        
        ServiceInstance newInstance = new ServiceInstance(op.getServiceId(), op.getServiceName(), op.getHost(), op.getPort());
        newInstance.setMetadata(op.getMetadata());
        newInstance.setLastHeartbeat(op.getTimestamp());
        
        instances.add(newInstance);
        
        notifyListeners(serviceName, instances);
        log.info("Service registered: {}/{}", serviceName, op.getServiceId());
    }

    private void handleDeregister(RegistryOperation op) {
        String serviceName = op.getServiceName();
        List<ServiceInstance> instances = registryStore.get(serviceName);
        
        if (instances != null) {
            boolean removed = instances.removeIf(i -> i.getServiceId().equals(op.getServiceId()));
            if (removed) {
                notifyListeners(serviceName, instances);
                log.info("Service deregistered: {}/{}", serviceName, op.getServiceId());
            }
        }
    }

    private void notifyListeners(String serviceName, List<ServiceInstance> instances) {
        List<ServiceListener> serviceListeners = listeners.get(serviceName);
        if (serviceListeners != null) {
            for (ServiceListener listener : serviceListeners) {
                try {
                    listener.onUpdate(serviceName, new ArrayList<>(instances));
                } catch (Exception e) {
                    log.error("Error notifying listener", e);
                }
            }
        }
    }

    public List<ServiceInstance> getInstances(String serviceName) {
        return registryStore.getOrDefault(serviceName, new ArrayList<>());
    }
    
    public List<String> getServices() {
        return new ArrayList<>(registryStore.keySet());
    }

    public void subscribe(String serviceName, ServiceListener listener) {
        listeners.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>()).add(listener);
        // Notify current state immediately
        List<ServiceInstance> current = registryStore.get(serviceName);
        if (current != null && !current.isEmpty()) {
            listener.onUpdate(serviceName, new ArrayList<>(current));
        }
    }

    public void unsubscribe(String serviceName, ServiceListener listener) {
        List<ServiceListener> serviceListeners = listeners.get(serviceName);
        if (serviceListeners != null) {
            serviceListeners.remove(listener);
        }
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        final String path = writer.getPath() + "/registry_data";
        try (FileOutputStream fos = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(registryStore);
            if (writer.addFile("registry_data")) {
                done.run(Status.OK());
            } else {
                done.run(new Status(RaftError.EIO, "Fail to add snapshot file"));
            }
        } catch (IOException e) {
            log.error("Fail to save snapshot", e);
            done.run(new Status(RaftError.EIO, "Fail to save snapshot: %s", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (reader.getFileMeta("registry_data") == null) {
            log.error("Fail to find registry_data file in snapshot");
            return false;
        }
        
        String path = reader.getPath() + "/registry_data";
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<String, List<ServiceInstance>> loaded = (Map<String, List<ServiceInstance>>) ois.readObject();
            registryStore.clear();
            registryStore.putAll(loaded);
            return true;
        } catch (Exception e) {
            log.error("Fail to load snapshot", e);
            return false;
        }
    }

    private RegistryOperation deserialize(ByteBuffer data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data.array(), data.position(), data.remaining());
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (RegistryOperation) ois.readObject();
        } catch (Exception e) {
            log.error("Fail to deserialize RegistryOperation", e);
            return null;
        }
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }
}

