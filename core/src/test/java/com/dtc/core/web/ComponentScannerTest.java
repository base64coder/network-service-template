package com.dtc.core.web;

import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ComponentScanner 测试
 */
@DisplayName("组件扫描器测试")
public class ComponentScannerTest {

    @Mock
    private Injector mockInjector;

    private ComponentScanner scanner;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        scanner = new ComponentScanner(mockInjector);
    }

    @Test
    @DisplayName("测试创建组件扫描器")
    void testCreateScanner() {
        assertNotNull(scanner);
    }

    @Test
    @DisplayName("测试扫描包")
    void testScanPackage() {
        assertDoesNotThrow(() -> 
            scanner.scanPackage("com.dtc.core.web"));
    }

    @Test
    @DisplayName("测试扫描空包名")
    void testScanEmptyPackage() {
        assertDoesNotThrow(() -> scanner.scanPackage(""));
    }

    @Test
    @DisplayName("测试扫描null包名")
    void testScanNullPackage() {
        assertDoesNotThrow(() -> scanner.scanPackage(null));
    }
}

