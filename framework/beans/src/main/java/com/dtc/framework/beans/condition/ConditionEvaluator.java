package com.dtc.framework.beans.condition;

import com.dtc.annotations.condition.Condition;
import com.dtc.annotations.condition.ConditionContext;
import com.dtc.annotations.condition.Conditional;
import com.dtc.framework.beans.BeanDefinitionReader;
import com.dtc.framework.beans.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;

/**
 * Evaluates @Conditional annotations.
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
                if (!condition.matches(context, Collections.emptyMap())) { // Metadata map simplification
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
        public BeanDefinitionReader getRegistry() {
            return registry;
        }
        
        @Override
        public Environment getEnvironment() {
            return environment;
        }
    }
}

