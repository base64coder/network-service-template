package com.dtc.core.persistence;

import com.dtc.core.persistence.impl.SimpleDataSourceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PersistenceManager 测试
 */
@DisplayName("持久化管理器测试")
public class PersistenceManagerTest {

    private PersistenceManager persistenceManager;

    @BeforeEach
    void setUp() {
        DataStore dataStore = new DataStore();
        persistenceManager = new PersistenceManager(dataStore);
    }

    @Test
    @DisplayName("测试创建持久化管理器")
    void testCreatePersistenceManager() {
        assertNotNull(persistenceManager);
    }

    @Test
    @DisplayName("测试启动持久化管理器")
    void testStart() throws Exception {
        var future = persistenceManager.start();
        assertNotNull(future);
        future.get();
        assertTrue(persistenceManager.isStarted());
    }

    @Test
    @DisplayName("测试停止持久化管理器")
    void testStop() throws Exception {
        persistenceManager.start().get();
        var future = persistenceManager.stop();
        assertNotNull(future);
        future.get();
        assertFalse(persistenceManager.isStarted());
    }

}

