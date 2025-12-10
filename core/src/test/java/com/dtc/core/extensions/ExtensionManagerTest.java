package com.dtc.core.extensions;

import com.dtc.api.Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionManager 测试
 */
@DisplayName("扩展管理器测试")
public class ExtensionManagerTest {

    private ExtensionManager extensionManager;

    @BeforeEach
    void setUp() {
        extensionManager = new ExtensionManager();
    }

    @Test
    @DisplayName("测试创建扩展管理器")
    void testCreateExtensionManager() {
        assertNotNull(extensionManager);
    }

    @Test
    @DisplayName("测试注册扩展")
    void testRegisterExtension() {
        TestExtension extension = new TestExtension();
        
        assertDoesNotThrow(() -> 
            extensionManager.registerExtension("test-ext", extension));
    }

    @Test
    @DisplayName("测试获取扩展")
    void testGetExtension() {
        TestExtension extension = new TestExtension();
        extensionManager.registerExtension("test-ext", extension);
        
        NetworkExtension retrieved = extensionManager.getExtension("test-ext");
        assertNotNull(retrieved);
    }

    @Test
    @DisplayName("测试获取所有扩展")
    void testGetAllExtensions() {
        TestExtension ext1 = new TestExtension();
        TestExtension ext2 = new TestExtension();
        
        extensionManager.registerExtension("ext1", ext1);
        extensionManager.registerExtension("ext2", ext2);
        
        var extensions = extensionManager.getAllExtensions();
        assertNotNull(extensions);
        assertTrue(extensions.size() >= 2);
    }

    @Test
    @DisplayName("测试注销扩展")
    void testUnregisterExtension() {
        TestExtension extension = new TestExtension();
        extensionManager.registerExtension("test-ext", extension);
        
        assertDoesNotThrow(() -> 
            extensionManager.unregisterExtension("test-ext"));
        
        NetworkExtension retrieved = extensionManager.getExtension("test-ext");
        assertNull(retrieved);
    }

    // 测试用扩展类
    static class TestExtension implements Extension {
        @Override
        public void initialize(Object... args) {
        }

        @Override
        public void start(Object input) {
        }

        @Override
        public void stop(Object input) {
        }

        @Override
        public String getExtensionId() {
            return "test-extension";
        }

        @Override
        public String getExtensionName() {
            return "Test Extension";
        }

        @Override
        public String getExtensionVersion() {
            return "1.0.0";
        }
    }
}

