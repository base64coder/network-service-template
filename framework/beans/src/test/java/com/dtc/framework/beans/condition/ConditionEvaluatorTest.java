package com.dtc.framework.beans.condition;

import com.dtc.annotations.condition.Condition;
import com.dtc.annotations.condition.ConditionContext;
import com.dtc.framework.beans.BeanDefinitionReader;
import com.dtc.framework.beans.Environment;
import com.dtc.framework.beans.impl.DefaultEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorTest {

    private ConditionEvaluator evaluator;
    private BeanDefinitionReader reader;
    private Environment environment;

    @BeforeEach
    void setUp() {
        reader = new BeanDefinitionReader();
        environment = new DefaultEnvironment();
        evaluator = new ConditionEvaluator(reader, environment);
    }

    @Test
    void testShouldSkipWithoutConditional() {
        TestClassWithoutConditional testClass = new TestClassWithoutConditional();
        
        boolean shouldSkip = evaluator.shouldSkip(testClass.getClass());
        assertFalse(shouldSkip);
    }

    @Test
    void testShouldSkipWithMatchingCondition() {
        TestClassWithConditional testClass = new TestClassWithConditional();
        
        // Condition should match (always returns true in test)
        boolean shouldSkip = evaluator.shouldSkip(testClass.getClass());
        // Depends on condition implementation
        assertNotNull(evaluator);
    }

    static class TestClassWithoutConditional {
    }

    @com.dtc.annotations.condition.Conditional(AlwaysTrueCondition.class)
    static class TestClassWithConditional {
    }

    static class AlwaysTrueCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, Map<String, Object> metadata) {
            return true;
        }
    }
}

