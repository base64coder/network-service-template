package com.dtc.framework.aop.support;

import com.dtc.framework.aop.ClassFilter;
import com.dtc.framework.aop.MethodMatcher;
import com.dtc.framework.aop.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationMatchingPointcut implements Pointcut, ClassFilter, MethodMatcher {
    private final Class<? extends Annotation> annotationType;

    public AnnotationMatchingPointcut(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return true; 
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 1. Check method directly
        if (method.isAnnotationPresent(annotationType)) {
            return true;
        }
        
        // 2. Check target class (class-level annotation)
        if (targetClass != null && targetClass.isAnnotationPresent(annotationType)) {
            return true;
        }

        // 3. Handle Proxy: Check method on target class
        if (targetClass != null && targetClass != method.getDeclaringClass()) {
            try {
                Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                if (targetMethod.isAnnotationPresent(annotationType)) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
                // Ignore
            }
        }
        
        return false;
    }
}
