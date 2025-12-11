package com.dtc.framework.beans.factory.bytecode;

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
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ByteBuddy 访问器生成工厂
 * 负责动态生成 BeanInstantiator 和 FieldAccessor
 */
public class ByteBuddyAccessorGenerator {
    private static final Logger log = LoggerFactory.getLogger(ByteBuddyAccessorGenerator.class);
    
    // 缓存生成的 Accessor，避免重复生成
    private final Map<Class<?>, BeanInstantiator> instantiatorCache = new ConcurrentHashMap<>();
    private final Map<Field, FieldAccessor> fieldAccessorCache = new ConcurrentHashMap<>();
    
    private final ByteBuddy byteBuddy = new ByteBuddy();

    /**
     * 获取或生成 Bean 实例化器
     */
    public BeanInstantiator getInstantiator(Class<?> beanClass) {
        return instantiatorCache.computeIfAbsent(beanClass, this::createInstantiator);
    }

    /**
     * 获取或生成字段访问器
     */
    public FieldAccessor getFieldAccessor(Field field) {
        return fieldAccessorCache.computeIfAbsent(field, this::createFieldAccessor);
    }

    private BeanInstantiator createInstantiator(Class<?> beanClass) {
        try {
            if (beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())) {
                return null;
            }

            return new ByteBuddy()
                    .subclass(BeanInstantiator.class)
                    .method(ElementMatchers.named("newInstance"))
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target implementationTarget) {
                            return (mv, context, instrumentedMethod) -> {
                                TypeDescription targetType = new TypeDescription.ForLoadedType(beanClass);
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        new StackManipulation() {
                                            @Override
                                            public boolean isValid() { return true; }
                                            @Override
                                            public Size apply(MethodVisitor mv, Implementation.Context implementationContext) {
                                                mv.visitTypeInsn(Opcodes.NEW, targetType.getInternalName());
                                                mv.visitInsn(Opcodes.DUP);
                                                return new Size(2, 2);
                                            }
                                        },
                                        net.bytebuddy.implementation.bytecode.member.MethodInvocation.invoke(
                                                targetType.getDeclaredMethods()
                                                        .filter(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(0)))
                                                        .getOnly()
                                        ),
                                        MethodReturn.REFERENCE
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
                    .load(beanClass.getClassLoader(), net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Throwable e) {
            // log.warn("Failed to generate ByteBuddy Instantiator for {}, falling back to reflection.", beanClass.getName(), e);
            return new ReflectionBeanInstantiator(beanClass);
        }
    }

    private FieldAccessor createFieldAccessor(Field field) {
        if (Modifier.isPrivate(field.getModifiers())) {
            return new ReflectionFieldAccessor(field);
        }

        try {
            Class<?> beanClass = field.getDeclaringClass();
            
            return new ByteBuddy()
                    .subclass(FieldAccessor.class)
                    .method(ElementMatchers.named("set"))
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target implementationTarget) {
                            return (mv, context, instrumentedMethod) -> {
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        MethodVariableAccess.REFERENCE.loadFrom(1), // bean
                                        TypeCasting.to(new TypeDescription.ForLoadedType(beanClass)),
                                        MethodVariableAccess.REFERENCE.loadFrom(2), // value
                                        TypeCasting.to(new TypeDescription.ForLoadedType(field.getType())),
                                        FieldAccess.forField(new FieldDescription.ForLoadedField(field)).write(),
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
                    .method(ElementMatchers.named("get"))
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target implementationTarget) {
                            return (mv, context, instrumentedMethod) -> {
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        MethodVariableAccess.REFERENCE.loadFrom(1), // bean
                                        TypeCasting.to(new TypeDescription.ForLoadedType(beanClass)),
                                        FieldAccess.forField(new FieldDescription.ForLoadedField(field)).read(),
                                        Assigner.DEFAULT.assign(
                                                new TypeDescription.ForLoadedType(field.getType()).asGenericType(),
                                                new TypeDescription.ForLoadedType(Object.class).asGenericType(),
                                                Assigner.Typing.STATIC
                                        ),
                                        MethodReturn.REFERENCE
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
                    .load(beanClass.getClassLoader(), net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Throwable e) {
            // log.warn("Failed to generate ByteBuddy FieldAccessor, falling back to reflection.", e);
            return new ReflectionFieldAccessor(field);
        }
    }

    // Fallback implementations
    private static class ReflectionBeanInstantiator implements BeanInstantiator {
        private final java.lang.reflect.Constructor<?> constructor;

        public ReflectionBeanInstantiator(Class<?> beanClass) {
            try {
                this.constructor = beanClass.getDeclaredConstructor();
                this.constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object newInstance() {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ReflectionFieldAccessor implements FieldAccessor {
        private final Field field;

        public ReflectionFieldAccessor(Field field) {
            this.field = field;
            this.field.setAccessible(true);
        }

        @Override
        public void set(Object bean, Object value) {
            try {
                field.set(bean, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get(Object bean) {
            try {
                return field.get(bean);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
