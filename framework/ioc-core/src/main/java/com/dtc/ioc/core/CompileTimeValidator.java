package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 编译期验证器
 * 提供编译期依赖验证功能
 * 
 * @author Network Service Template
 */
public class CompileTimeValidator {
    
    private static final Logger log = LoggerFactory.getLogger(CompileTimeValidator.class);
    
    /**
     * 验证类的依赖
     * 
     * @param clazz 要验证的类
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateDependencies(@NotNull Class<?> clazz) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 验证字段依赖
            validateFieldDependencies(clazz, result);
            
            // 验证构造函数依赖
            validateConstructorDependencies(clazz, result);
            
            // 检测循环依赖
            detectCircularDependencies(clazz, result);
            
        } catch (Exception e) {
            result.addError("Validation failed: " + e.getMessage());
            log.error("❌ Validation failed for class: {}", clazz.getName(), e);
        }
        
        return result;
    }
    
    /**
     * 验证字段依赖
     */
    private static void validateFieldDependencies(Class<?> clazz, ValidationResult result) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(com.dtc.ioc.annotations.Autowired.class)) {
                Class<?> fieldType = field.getType();
                
                // 检查字段类型是否可解析
                if (!isResolvableType(fieldType)) {
                    result.addError("Unresolvable dependency for field: " + field.getName() + " of type " + fieldType.getName());
                }
                
                // 检查字段访问性
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
            }
        }
    }
    
    /**
     * 验证构造函数依赖
     */
    private static void validateConstructorDependencies(Class<?> clazz, ValidationResult result) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                
                // 检查参数类型是否可解析
                if (!isResolvableType(paramType)) {
                    result.addError("Unresolvable constructor parameter: " + paramType.getName() + " at index " + i);
                }
            }
        }
    }
    
    /**
     * 检测循环依赖
     */
    private static void detectCircularDependencies(Class<?> clazz, ValidationResult result) {
        List<Class<?>> dependencyChain = new ArrayList<>();
        detectCircularDependenciesRecursive(clazz, dependencyChain, result);
    }
    
    /**
     * 递归检测循环依赖
     */
    private static void detectCircularDependenciesRecursive(Class<?> clazz, List<Class<?>> chain, ValidationResult result) {
        if (chain.contains(clazz)) {
            StringBuilder cycle = new StringBuilder();
            for (Class<?> c : chain) {
                cycle.append(c.getSimpleName()).append(" -> ");
            }
            cycle.append(clazz.getSimpleName());
            result.addError("Circular dependency detected: " + cycle.toString());
            return;
        }
        
        chain.add(clazz);
        
        // 检查字段依赖
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(com.dtc.ioc.annotations.Autowired.class)) {
                Class<?> fieldType = field.getType();
                if (isBeanType(fieldType)) {
                    detectCircularDependenciesRecursive(fieldType, new ArrayList<>(chain), result);
                }
            }
        }
        
        chain.remove(chain.size() - 1);
    }
    
    /**
     * 检查类型是否可解析
     */
    private static boolean isResolvableType(Class<?> type) {
        // 基本类型和包装类型
        if (type.isPrimitive() || isWrapperType(type)) {
            return true;
        }
        
        // 字符串类型
        if (type == String.class) {
            return true;
        }
        
        // 数组类型
        if (type.isArray()) {
            return isResolvableType(type.getComponentType());
        }
        
        // 集合类型
        if (java.util.Collection.class.isAssignableFrom(type) || 
            java.util.Map.class.isAssignableFrom(type)) {
            return true;
        }
        
        // Bean类型
        return isBeanType(type);
    }
    
    /**
     * 检查是否为包装类型
     */
    private static boolean isWrapperType(Class<?> type) {
        return type == Boolean.class || type == Byte.class || type == Character.class ||
               type == Short.class || type == Integer.class || type == Long.class ||
               type == Float.class || type == Double.class;
    }
    
    /**
     * 检查是否为Bean类型
     */
    private static boolean isBeanType(Class<?> type) {
        // 检查是否有@Component注解
        return type.isAnnotationPresent(com.dtc.ioc.annotations.Component.class) ||
               type.isAnnotationPresent(com.dtc.ioc.annotations.Service.class) ||
               type.isAnnotationPresent(com.dtc.ioc.annotations.Repository.class);
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result:\n");
            
            if (errors.isEmpty()) {
                sb.append("✅ No errors found\n");
            } else {
                sb.append("❌ Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("⚠️ Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}
