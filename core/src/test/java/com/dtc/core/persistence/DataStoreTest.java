package com.dtc.core.persistence;

import com.dtc.core.persistence.impl.SimpleDataSourceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStore 测试
 */
@DisplayName("数据存储测试")
public class DataStoreTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
    }

    @Test
    @DisplayName("测试创建数据存储")
    void testCreateDataStore() {
        assertNotNull(dataStore);
    }

    @Test
    @DisplayName("测试初始化数据存储")
    void testInitialize() throws Exception {
        assertDoesNotThrow(() -> dataStore.initialize());
    }

    @Test
    @DisplayName("测试关闭数据存储")
    void testShutdown() throws Exception {
        dataStore.initialize();
        assertDoesNotThrow(() -> dataStore.shutdown());
    }

    @Test
    @DisplayName("测试存储和获取数据")
    void testStoreAndGet() {
        dataStore.put("key1", "value1");
        assertEquals("value1", dataStore.get("key1"));
    }

    @Test
    @DisplayName("测试删除数据")
    void testRemove() {
        dataStore.put("key1", "value1");
        Object removed = dataStore.remove("key1");
        assertEquals("value1", removed);
        assertNull(dataStore.get("key1"));
    }

    @Test
    @DisplayName("测试检查键是否存在")
    void testContainsKey() {
        assertFalse(dataStore.containsKey("key1"));
        dataStore.put("key1", "value1");
        assertTrue(dataStore.containsKey("key1"));
    }

    @Test
    @DisplayName("测试获取所有数据")
    void testGetAllData() {
        dataStore.put("key1", "value1");
        dataStore.put("key2", "value2");
        var allData = dataStore.getAllData();
        assertEquals(2, allData.size());
        assertEquals("value1", allData.get("key1"));
        assertEquals("value2", allData.get("key2"));
    }

    @Test
    @DisplayName("测试是否已初始化")
    void testIsInitialized() throws Exception {
        assertFalse(dataStore.isInitialized());
        dataStore.initialize();
        assertTrue(dataStore.isInitialized());
    }
}

