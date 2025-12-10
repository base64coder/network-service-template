package com.dtc.framework.distributed.storage;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 H2 的分布式存储状态机
 */
public class H2StateMachine extends StateMachineAdapter {

    private static final Logger log = LoggerFactory.getLogger(H2StateMachine.class);
    private final H2StorageEngine storageEngine;
    private final AtomicLong leaderTerm = new AtomicLong(-1);

    public H2StateMachine(H2StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    @Override
    public void onApply(Iterator iter) {
        while (iter.hasNext()) {
            ByteBuffer data = iter.getData();
            Closure done = iter.done();

            try {
                SqlOperation op = deserialize(data);
                if (op != null) {
                    // 执行 SQL
                    storageEngine.update(op.getSql(), op.getParams()).join();
                }

                if (done != null) {
                    done.run(Status.OK());
                }
            } catch (Exception e) {
                log.error("Fail to apply sql operation", e);
                if (done != null) {
                    done.run(new Status(RaftError.EINTERNAL, "Fail to apply operation: %s", e.getMessage()));
                }
            }

            iter.next();
        }
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        final String fileName = "h2_dump.sql.zip";
        final String path = writer.getPath() + File.separator + fileName;
        
        try {
            storageEngine.backup(path);
            if (writer.addFile(fileName)) {
                done.run(Status.OK());
            } else {
                done.run(new Status(RaftError.EIO, "Fail to add snapshot file: " + fileName));
            }
        } catch (Exception e) {
            log.error("Fail to save snapshot", e);
            done.run(new Status(RaftError.EIO, "Fail to save snapshot: %s", e.getMessage()));
        }
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        final String fileName = "h2_dump.sql.zip";
        if (reader.getFileMeta(fileName) == null) {
            log.error("Fail to find snapshot file: {}", fileName);
            return false;
        }

        String path = reader.getPath() + File.separator + fileName;
        try {
            storageEngine.restore(path);
            return true;
        } catch (Exception e) {
            log.error("Fail to load snapshot", e);
            return false;
        }
    }

    @Override
    public void onLeaderStart(long term) {
        this.leaderTerm.set(term);
        super.onLeaderStart(term);
    }

    @Override
    public void onLeaderStop(Status status) {
        this.leaderTerm.set(-1);
        super.onLeaderStop(status);
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    private SqlOperation deserialize(ByteBuffer data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data.array(), data.position(), data.remaining());
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (SqlOperation) ois.readObject();
        } catch (Exception e) {
            log.error("Fail to deserialize SqlOperation", e);
            return null;
        }
    }
}

