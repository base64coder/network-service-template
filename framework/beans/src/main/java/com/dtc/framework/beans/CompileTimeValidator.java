package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
     * ç¼è¯æéªè¯å¨
æä¾ç¼è¯æä¾èµéªè¯åè½
@author Network Service Template
/
public class CompileTimeValidator {
    
    private static final Logger log = LoggerFactory.getLogger(CompileTimeValidator.class);
    
    /**
     * éªè¯ç±»çä¾èµ
@param clazz è¦éªè¯çç±»
@return éªè¯ç»æ
/
    @NotNull
    public static ValidationResult validateDependencies(@NotNull Class<?> clazz) {
        ValidationResult result = new ValidationResult();
        
        try {
            // éªè¯å­æ®µä¾èµ
            validateFieldDependencies(clazz, result);
            
            // éªè¯æé å½æ°ä¾èµ
            validateConstructorDependencies(clazz, result);
            
            // æ£æµå¾ªç¯ä¾èµ
            detectCircularDependencies(clazz, result);
            
        } catch (Exception e) {
            result.addError("Validation failed: " + e.getMessage());
            log.error("â Validation failed for class: {}", clazz.getName(), e);
        }
        
        return result;
    }
    
    /**
     * éªè¯å­æ®µä¾èµ
/
    private static void validateFieldDependencies(Class<?> clazz, ValidationResult result) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(com.dtc.annotations.ioc.Autowired.class)) {
                Class<?> fieldType = field.getType();
                
                // æ£æ¥å­æ®µç±»åæ¯å¦å¯è§£æ
                if (!isResolvableType(fieldType)) {
                    result.addError("Unresolvable dependency for field: " + field.getName() + " of type " + fieldType.getName());
                }
                
                // æ£æ¥å­æ®µè®¿é®æ§
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
            }
        }
    }
    
    /**
     * éªè¯æé å½æ°ä¾èµ
/
    private static void validateConstructorDependencies(Class<?> clazz, ValidationResult result) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                
                // æ£æ¥åæ°ç±»åæ¯å¦å¯è§£æ
                if (!isResolvableType(paramType)) {
                    result.addError("Unresolvable constructor parameter: " + paramType.getName() + " at index " + i);
                }
            }
        }
    }
    
    /**
     * æ£æµå¾ªç¯ä¾èµ
/
    private static void detectCircularDependencies(Class<?> clazz, ValidationResult result) {
        List<Class<?>> dependencyChain = new ArrayList<>();
        detectCircularDependenciesRecursive(clazz, dependencyChain, result);
    }
    
    /**
     * éå½æ£æµå¾ªç¯ä¾èµ
/
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
        
        // æ£æ¥å­æ®µä¾èµ
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(com.dtc.annotations.ioc.Autowired.class)) {
                Class<?> fieldType = field.getType();
                if (isBeanType(fieldType)) {
                    detectCircularDependenciesRecursive(fieldType, new ArrayList<>(chain), result);
                }
            }
        }
        
        chain.remove(chain.size() - 1);
    }
    
    /**
     * æ£æ¥ç±»åæ¯å¦å¯è§£æ
/
    private static boolean isResolvableType(Class<?> type) {
        // åºæ¬ç±»åååè£ç±»å
        if (type.isPrimitive() || isWrapperType(type)) {
            return true;
        }
        
        // å­ç¬¦ä¸²ç±»å
        if (type == String.class) {
            return true;
        }
        
        // æ°ç»ç±»å
        if (type.isArray()) {
            return isResolvableType(type.getComponentType());
        }
        
        // éåç±»å
        if (java.util.Collection.class.isAssignableFrom(type) || 
            java.util.Map.class.isAssignableFrom(type)) {
            return true;
        }
        
        // Beanç±»å
        return isBeanType(type);
    }
    
    /**
     * æ£æ¥æ¯å¦ä¸ºåè£ç±»å
/
    private static boolean isWrapperType(Class<?> type) {
        return type == Boolean.class || type == Byte.class || type == Character.class ||
               type == Short.class || type == Integer.class || type == Long.class ||
               type == Float.class || type == Double.class;
    }
    
    /**
     * æ£æ¥æ¯å¦ä¸ºBeanç±»å
/
    private static boolean isBeanType(Class<?> type) {
        // æ£æ¥æ¯å¦æ@Componentæ³¨è§£
        return type.isAnnotationPresent(com.dtc.annotations.ioc.Component.class) ||
               type.isAnnotationPresent(com.dtc.annotations.ioc.Service.class) ||
               type.isAnnotationPresent(com.dtc.annotations.ioc.Repository.class);
    }
    
    /**
     * éªè¯ç»æç±»
/
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
                sb.append("â No errors found\n");
            } else {
                sb.append("â Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("â ï¸ Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}
