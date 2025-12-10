package com.dtc.core.validation;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AnnotationValidator 测试
 */
@DisplayName("注解验证器测试")
public class AnnotationValidatorTest {

    @Test
    @DisplayName("测试验证NotNull参数")
    void testValidateNotNullParameter() throws Exception {
        Method method = TestClass.class.getMethod("testMethod", String.class);
        Object[] args = {"test"};
        
        assertDoesNotThrow(() -> 
            AnnotationValidator.validateMethodParameters(method, args));
    }

    @Test
    @DisplayName("测试验证null参数抛出异常")
    void testValidateNullParameter() throws Exception {
        Method method = TestClass.class.getMethod("testMethod", String.class);
        Object[] args = {null};
        
        assertThrows(IllegalArgumentException.class, () -> 
            AnnotationValidator.validateMethodParameters(method, args));
    }

    @Test
    @DisplayName("测试验证NotNull返回值")
    void testValidateNotNullReturnValue() throws Exception {
        Method method = TestClass.class.getMethod("returnValue");
        
        assertDoesNotThrow(() -> 
            AnnotationValidator.validateMethodReturnValue(method, "result"));
    }

    @Test
    @DisplayName("测试验证null返回值抛出异常")
    void testValidateNullReturnValue() throws Exception {
        Method method = TestClass.class.getMethod("returnValue");
        
        assertThrows(IllegalArgumentException.class, () -> 
            AnnotationValidator.validateMethodReturnValue(method, null));
    }

    @Test
    @DisplayName("测试检查是否需要参数验证")
    void testNeedsParameterValidation() throws Exception {
        Method method = TestClass.class.getMethod("testMethod", String.class);
        
        boolean needs = AnnotationValidator.needsParameterValidation(method);
        assertTrue(needs);
    }

    @Test
    @DisplayName("测试无注解方法不需要验证")
    void testNoValidationNeeded() throws Exception {
        Method method = TestClass.class.getMethod("noValidation", String.class);
        
        boolean needs = AnnotationValidator.needsParameterValidation(method);
        assertFalse(needs);
    }

    // 测试类
    static class TestClass {
        public void testMethod(@NotNull String param) {
        }

        @NotNull
        public String returnValue() {
            return "test";
        }

        public void noValidation(String param) {
        }
    }
}

