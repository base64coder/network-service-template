package com.dtc.framework.distributed.storage;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class H2StateMachineTest {

    private H2StorageEngine storageEngine;
    private H2StateMachine stateMachine;

    @BeforeEach
    void setUp() {
        storageEngine = mock(H2StorageEngine.class);
        stateMachine = new H2StateMachine(storageEngine);
    }

    @Test
    void testOnApplySuccess() throws Exception {
        // Prepare data
        String sql = "INSERT INTO test VALUES (?, ?)";
        Object[] params = new Object[]{"a", 1};
        SqlOperation op = new SqlOperation(sql, params);
        ByteBuffer data = ByteBuffer.wrap(serialize(op));

        // Mock Iterator
        Iterator iter = mock(Iterator.class);
        when(iter.hasNext()).thenReturn(true, false);
        when(iter.getData()).thenReturn(data);
        Closure done = mock(Closure.class);
        when(iter.done()).thenReturn(done);

        // Mock Engine
        when(storageEngine.update(anyString(), any())).thenReturn(CompletableFuture.completedFuture(1));

        // Execute
        stateMachine.onApply(iter);

        // Verify
        verify(storageEngine).update(eq(sql), any());
        verify(done).run(argThat(Status::isOk));
    }

    @Test
    void testOnApplyFailure() throws Exception {
        // Prepare data
        SqlOperation op = new SqlOperation("BAD SQL", null);
        ByteBuffer data = ByteBuffer.wrap(serialize(op));

        // Mock Iterator
        Iterator iter = mock(Iterator.class);
        when(iter.hasNext()).thenReturn(true, false);
        when(iter.getData()).thenReturn(data);
        Closure done = mock(Closure.class);
        when(iter.done()).thenReturn(done);

        // Mock Engine Exception
        when(storageEngine.update(anyString(), any())).thenThrow(new RuntimeException("DB Error"));

        // Execute
        stateMachine.onApply(iter);

        // Verify
        verify(done).run(argThat(status -> !status.isOk()));
    }

    @Test
    void testOnSnapshotSave(@TempDir Path tempDir) throws Exception {
        SnapshotWriter writer = mock(SnapshotWriter.class);
        when(writer.getPath()).thenReturn(tempDir.toString());
        when(writer.addFile(anyString())).thenReturn(true);
        Closure done = mock(Closure.class);

        stateMachine.onSnapshotSave(writer, done);

        verify(storageEngine).backup(contains("h2_dump.sql.zip"));
        verify(writer).addFile("h2_dump.sql.zip");
        verify(done).run(argThat(Status::isOk));
    }

    @Test
    void testOnSnapshotLoadSuccess(@TempDir Path tempDir) throws Exception {
        SnapshotReader reader = mock(SnapshotReader.class);
        when(reader.getPath()).thenReturn(tempDir.toString());
        // Mock meta existence check usually returns object, here logic checks !null
        
        // Need to mock getFileMeta to return non-null to proceed
        // Using raw mock object
        Message meta = mock(Message.class);
        when(reader.getFileMeta("h2_dump.sql.zip")).thenReturn(meta);

        boolean success = stateMachine.onSnapshotLoad(reader);

        verify(storageEngine).restore(contains("h2_dump.sql.zip"));
        assertTrue(success);
    }

    @Test
    void testOnSnapshotLoadFail() throws Exception {
        SnapshotReader reader = mock(SnapshotReader.class);
        when(reader.getFileMeta("h2_dump.sql.zip")).thenReturn(null);

        boolean success = stateMachine.onSnapshotLoad(reader);

        verify(storageEngine, never()).restore(anyString());
        assertFalse(success);
    }
    
    @Test
    void testLeaderLifecycle() {
        assertFalse(stateMachine.isLeader());
        stateMachine.onLeaderStart(1);
        assertTrue(stateMachine.isLeader());
        stateMachine.onLeaderStop(Status.OK());
        assertFalse(stateMachine.isLeader());
    }

    private byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        return bos.toByteArray();
    }
}

