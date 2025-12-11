package com.dtc.framework.beans.factory.accessor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ByteBuddy 访问器工厂
 * 负责动态生成高性能的 Accessor 并进行缓存
 */
public class ByteBuddyAccessorFactory {
    private static final Logger log = LoggerFactory.getLogger(ByteBuddyAccessorFactory.class);
    
    // 缓存生成的 Accessor，避免重复生成
    private static final Map<Field, FieldAccessor> fieldAccessorCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, BeanAccessor> beanAccessorCache = new ConcurrentHashMap<>();
    
    private static final ByteBuddy byteBuddy = new ByteBuddy();

    /**
     * 获取或生成 FieldAccessor
     */
    public static FieldAccessor getFieldAccessor(Field field) {
        return fieldAccessorCache.computeIfAbsent(field, ByteBuddyAccessorFactory::createFieldAccessor);
    }

    /**
     * 获取或生成 BeanAccessor
     */
    public static BeanAccessor getBeanAccessor(Class<?> beanClass) {
        return beanAccessorCache.computeIfAbsent(beanClass, ByteBuddyAccessorFactory::createBeanAccessor);
    }

    private static FieldAccessor createFieldAccessor(Field field) {
        try {
            Class<?> targetType = field.getDeclaringClass();
            return (FieldAccessor) byteBuddy
                    .subclass(FieldAccessor.class)
                    .method(ElementMatchers.named("set"))
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target implementationTarget) {
                            return (mv, context, instrumentedMethod) -> {
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        MethodVariableAccess.REFERENCE.loadFrom(1), // load target
                                        TypeCasting.to(new TypeDescription.ForLoadedType(targetType)), // cast to Target
                                        MethodVariableAccess.REFERENCE.loadFrom(2), // load value
                                        Assigner.DEFAULT.assign(
                                                new TypeDescription.ForLoadedType(Object.class).asGenericType(),
                                                new TypeDescription.ForLoadedType(field.getType()).asGenericType(),
                                                Assigner.Typing.DYNAMIC
                                        ), // cast/unbox value
                                        FieldAccess.forField(new FieldDescription.ForLoadedField(field)).write(), // set field
                                        MethodReturn.VOID
                                ).apply(mv, context);
                                return new ByteCodeAppender.Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
                            };
                        }

                        @Override
                        public InstrumentedType prepare(InstrumentedType instrumentedType) {
                            return instrumentedType;
                        }
                    })
                    .make()
                    .load(targetType.getClassLoader(), net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            log.warn("Failed to generate ByteBuddy FieldAccessor for field {}, falling back to reflection.", field, e);
            // 降级策略：返回一个基于反射的实现
            return (target, value) -> {
                try {
                    field.setAccessible(true);
                    field.set(target, value);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            };
        }
    }

    private static BeanAccessor createBeanAccessor(Class<?> beanClass) {
        try {
            Constructor<?> constructor = beanClass.getDeclaredConstructor();
            return (BeanAccessor) byteBuddy
                    .subclass(BeanAccessor.class)
                    .method(ElementMatchers.named("newInstance"))
                    .intercept(MethodCall.construct(constructor))
                    .make()
                    .load(beanClass.getClassLoader(), net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            log.warn("Failed to generate ByteBuddy BeanAccessor for class {}, falling back to reflection.", beanClass.getName(), e);
            return () -> {
                try {
                    return beanClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            };
        }
    }
}

