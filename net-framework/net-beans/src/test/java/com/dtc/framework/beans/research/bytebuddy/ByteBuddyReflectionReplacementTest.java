package com.dtc.framework.beans.research.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
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
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.field.FieldDescription;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 研究使用 ByteBuddy 替代反射进行字段注入
 */
public class ByteBuddyReflectionReplacementTest {

    // 目标 Bean
    public static class TargetBean {
        public String name;
        private int age; // 私有字段
    }

    // 定义一个高性能访问器接口
    public interface FieldAccessor {
        void set(Object target, Object value);
        Object get(Object target);
    }

    @Test
    public void testFieldAccessReplacement() throws Exception {
        TargetBean bean = new TargetBean();
        Field nameField = TargetBean.class.getField("name");

        // 1. 生成 Accessor
        FieldAccessor accessor = createAccessor(TargetBean.class, nameField);

        // 2. 测试 Set
        accessor.set(bean, "ByteBuddy");
        assertEquals("ByteBuddy", bean.name);

        // 3. 测试 Get
        Object value = accessor.get(bean);
        assertEquals("ByteBuddy", value);
        
        System.out.println("ByteBuddy Field Accessor Success!");
    }

    @Test
    public void benchmark() throws Exception {
        TargetBean bean = new TargetBean();
        Field nameField = TargetBean.class.getField("name");
        FieldAccessor accessor = createAccessor(TargetBean.class, nameField);
        
        // 预热
        for (int i = 0; i < 10000; i++) {
            nameField.set(bean, "Warmup");
            accessor.set(bean, "Warmup");
        }
        
        int iterations = 10_000_000; // 1千万次
        
        // 1. 反射测试
        long startReflect = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            nameField.set(bean, "Reflection");
        }
        long endReflect = System.nanoTime();
        
        // 2. ByteBuddy 测试
        long startByteBuddy = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            accessor.set(bean, "ByteBuddy");
        }
        long endByteBuddy = System.nanoTime();
        
        // 3. 原生调用测试 (基准)
        long startNative = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bean.name = "Native";
        }
        long endNative = System.nanoTime();
        
        double reflectMs = (endReflect - startReflect) / 1_000_000.0;
        double byteBuddyMs = (endByteBuddy - startByteBuddy) / 1_000_000.0;
        double nativeMs = (endNative - startNative) / 1_000_000.0;
        
        System.out.printf("Benchmark Result (%d iterations):%n", iterations);
        System.out.printf("Reflection: %.2f ms%n", reflectMs);
        System.out.printf("ByteBuddy : %.2f ms%n", byteBuddyMs);
        System.out.printf("Native    : %.2f ms%n", nativeMs);
        System.out.printf("ByteBuddy is %.2fx faster than Reflection%n", reflectMs / byteBuddyMs);
        System.out.printf("Overhead vs Native: ByteBuddy (+%.2f ms), Reflection (+%.2f ms)%n", 
                byteBuddyMs - nativeMs, reflectMs - nativeMs);
    }

    /**
     * 使用 ByteBuddy 动态生成 FieldAccessor 的实现类
     */
    private FieldAccessor createAccessor(Class<?> targetType, Field field) throws Exception {
        return new ByteBuddy()
                .subclass(FieldAccessor.class)
                .method(ElementMatchers.named("set"))
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        return new ByteCodeAppender() {
                            @Override
                            public Size apply(MethodVisitor mv, Context context, MethodDescription instrumentedMethod) {
                                // 生成: ((TargetBean) arg0).field = (FieldType) arg1;
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        MethodVariableAccess.REFERENCE.loadFrom(1), // 加载 arg0 (target)
                                        TypeCasting.to(new TypeDescription.ForLoadedType(targetType)), // 强转为 TargetBean
                                        MethodVariableAccess.REFERENCE.loadFrom(2), // 加载 arg1 (value)
                                        TypeCasting.to(new TypeDescription.ForLoadedType(field.getType())), // 强转为字段类型
                                        FieldAccess.forField(new FieldDescription.ForLoadedField(field)).write(), // 写入字段
                                        MethodReturn.VOID
                                ).apply(mv, context);
                                return new Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
                            }
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
                        return new ByteCodeAppender() {
                            @Override
                            public Size apply(MethodVisitor mv, Context context, MethodDescription instrumentedMethod) {
                                // 生成: return ((TargetBean) arg0).field;
                                StackManipulation.Size size = new StackManipulation.Compound(
                                        MethodVariableAccess.REFERENCE.loadFrom(1), // 加载 arg0
                                        TypeCasting.to(new TypeDescription.ForLoadedType(targetType)), // 强转
                                        FieldAccess.forField(new FieldDescription.ForLoadedField(field)).read(), // 读取
                                        Assigner.DEFAULT.assign(
                                                new TypeDescription.ForLoadedType(field.getType()).asGenericType(),
                                                new TypeDescription.ForLoadedType(Object.class).asGenericType(),
                                                Assigner.Typing.STATIC
                                        ), // 装箱 (如果需要)
                                        MethodReturn.REFERENCE
                                ).apply(mv, context);
                                return new Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
                            }
                        };
                    }

                    @Override
                    public InstrumentedType prepare(InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }
                })
                .make()
                .load(targetType.getClassLoader())
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }
}

