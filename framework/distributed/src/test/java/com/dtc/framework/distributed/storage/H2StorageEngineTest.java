package com.dtc.framework.distributed.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class H2StorageEngineTest {

    private H2StorageEngine engine;

    @BeforeEach
    void setUp() {
        engine = new H2StorageEngine();
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    void testUpdateAndQuery() throws ExecutionException, InterruptedException {
        // Insert
        String insertSql = "INSERT INTO kv_store (k, v, ts) VALUES (?, ?, ?)";
        int rows = engine.update(insertSql, "key1", "value1", 1000L).get();
        assertEquals(1, rows);

        // Query
        List<String> results = engine.query("SELECT * FROM kv_store WHERE k = ?", "key1");
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("key1"));
        assertTrue(results.get(0).contains("value1"));
    }

    @Test
    void testQueryEmpty() {
        List<String> results = engine.query("SELECT * FROM kv_store WHERE k = ?", "non_exist");
        assertTrue(results.isEmpty());
    }

    @Test
    void testBackupAndRestore(@TempDir Path tempDir) throws Exception {
        // Prepare data
        engine.update("INSERT INTO kv_store (k, v, ts) VALUES (?, ?, ?)", "key_backup", "val_backup", 2000L).get();
        
        // Backup
        String backupPath = tempDir.resolve("backup.sql.zip").toAbsolutePath().toString();
        engine.backup(backupPath);
        
        // Modify data (delete)
        engine.update("DELETE FROM kv_store WHERE k = ?", "key_backup").get();
        assertTrue(engine.query("SELECT * FROM kv_store WHERE k = ?", "key_backup").isEmpty());
        
        // Restore
        engine.restore(backupPath);
        
        // Verify restored
        List<String> results = engine.query("SELECT * FROM kv_store WHERE k = ?", "key_backup");
        assertEquals(1, results.size());
    }

    @Test
    void testInvalidSql() {
        assertThrows(ExecutionException.class, () -> {
            engine.update("INSERT INTO non_exist_table VALUES (1)").get();
        });
        
        assertThrows(RuntimeException.class, () -> {
            engine.query("SELECT * FROM non_exist_table");
        });
    }
}

