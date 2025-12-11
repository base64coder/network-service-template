package com.dtc.framework.beans.factory.support;

import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.beans.exception.BeanCreationException;
import com.dtc.framework.beans.factory.BeanFactory;
import com.dtc.framework.beans.factory.accessor.ByteBuddyAccessorFactory;
import com.dtc.framework.beans.factory.accessor.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 注入元数据
 * 缓存类的注入点信息，避免重复反射扫描
 */
public class InjectionMetadata {
    private static final Logger log = LoggerFactory.getLogger(InjectionMetadata.class);
    
    private final Class<?> targetClass;
    private final List<InjectedElement> injectedElements;

    public InjectionMetadata(Class<?> targetClass, List<InjectedElement> injectedElements) {
        this.targetClass = targetClass;
        this.injectedElements = injectedElements;
    }

    public void inject(Object target, String beanName, BeanFactory beanFactory) throws Throwable {
        if (injectedElements.isEmpty()) {
            return;
        }
        for (InjectedElement element : injectedElements) {
            element.inject(target, beanName, beanFactory);
        }
    }

    public static InjectionMetadata forClass(Class<?> clazz) {
        List<InjectedElement> elements = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    // 使用 ByteBuddy 生成高性能 Accessor
                    FieldAccessor accessor = ByteBuddyAccessorFactory.getFieldAccessor(field);
                    elements.add(new InjectedField(field, accessor));
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new InjectionMetadata(clazz, elements);
    }

    public static abstract class InjectedElement {
        protected final java.lang.reflect.Member member;

        protected InjectedElement(java.lang.reflect.Member member) {
            this.member = member;
        }

        protected abstract void inject(Object target, String beanName, BeanFactory beanFactory) throws Throwable;
    }

    private static class InjectedField extends InjectedElement {
        private final Field field;
        private final FieldAccessor fieldAccessor;

        public InjectedField(Field field, FieldAccessor fieldAccessor) {
            super(field);
            this.field = field;
            this.fieldAccessor = fieldAccessor;
        }

        @Override
        protected void inject(Object target, String beanName, BeanFactory beanFactory) throws Throwable {
            Object value = beanFactory.getBean(field.getType());
            if (value != null) {
                // 使用 ByteBuddy Accessor 设置属性，无反射开销
                fieldAccessor.set(target, value);
            }
        }
    }
}

