package com.dtc.ioc.core.condition;

import com.dtc.annotations.condition.Condition;
import com.dtc.annotations.condition.ConditionContext;
import com.dtc.annotations.condition.Conditional;
import com.dtc.ioc.core.BeanDefinitionReader;
import com.dtc.ioc.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;

/**
 * 条件评估器
 * 评估 @Conditional 注解
 * 
 * @author Network Service Template
 */
public class ConditionEvaluator {
    
    private static final Logger log = LoggerFactory.getLogger(ConditionEvaluator.class);
    
    private final ConditionContext context;
    
    public ConditionEvaluator(BeanDefinitionReader registry, Environment environment) {
        this.context = new ConditionContextImpl(registry, environment);
    }
    
    public boolean shouldSkip(AnnotatedElement metadata) {
        if (!metadata.isAnnotationPresent(Conditional.class)) {
            return false;
        }
        
        Conditional conditional = metadata.getAnnotation(Conditional.class);
        for (Class<? extends Condition> conditionClass : conditional.value()) {
            try {
                Condition condition = conditionClass.getDeclaredConstructor().newInstance();
                if (!condition.matches(context, Collections.emptyMap())) {
                    log.debug("Condition {} did not match for element {}", conditionClass.getSimpleName(), metadata);
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to instantiate Condition class: {}", conditionClass.getName(), e);
                return true; // Skip on error
            }
        }
        
        return false;
    }
    
    private static class ConditionContextImpl implements ConditionContext {
        private final BeanDefinitionReader registry;
        private final Environment environment;
        
        public ConditionContextImpl(BeanDefinitionReader registry, Environment environment) {
            this.registry = registry;
            this.environment = environment;
        }
        
        @Override
        public Object getRegistry() {
            return registry;
        }
        
        @Override
        public Object getEnvironment() {
            return environment;
        }
        
        @Override
        public String getProperty(String key) {
            return environment.getProperty(key);
        }
        
        @Override
        public String getProperty(String key, String defaultValue) {
            return environment.getProperty(key, defaultValue);
        }
        
        @Override
        public boolean containsBean(String beanName) {
            return registry.containsBeanDefinition(beanName);
        }
        
        @Override
        public Map<String, String> getAllProperties() {
            Map<String, Object> props = environment.getAllProperties();
            Map<String, String> result = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                result.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
            return result;
        }
    }
}
