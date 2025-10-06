package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.model.ExtensionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 扩展创建管理器
 * 统一管理扩展的创建、增强和生命周期
 * 
 * @author Network Service Template
 */
@Singleton
public class ExtensionCreationManager {

    private static final Logger log = LoggerFactory.getLogger(ExtensionCreationManager.class);

    @Inject
    public ExtensionCreationManager() {
    }

    /**
     * 创建增强的扩展实例
     * 
     * @param extensionClass 扩展类
     * @param classLoader    类加载器
     * @param extensionId    扩展ID
     * @param args           构造函数参数
     * @return 增强的扩展实例
     */
    @NotNull
    public <T> T createEnhancedExtension(@NotNull Class<T> extensionClass,
            @NotNull ClassLoader classLoader,
            @NotNull String extensionId,
            Object... args) {
        validateInput(extensionClass, "Extension class cannot be null");
        validateInput(classLoader, "ClassLoader cannot be null");
        validateInput(extensionId, "Extension ID cannot be null");

        try {
            log.debug("Creating enhanced extension: {} with ID: {} using layered strategy",
                    extensionClass.getName(), extensionId);

            // 使用 ByteBuddyFactory 的分层策略
            T instance = ByteBuddyFactory.getOrCreateInstance(extensionClass, classLoader, extensionId, args);

            // 验证实例
            validateInstance(instance, extensionClass.getName());

            log.info("Successfully created enhanced extension: {} with ID: {} using layered strategy",
                    extensionClass.getName(), extensionId);

            return instance;

        } catch (Exception e) {
            log.error("Failed to create enhanced extension: {} with ID: {} using layered strategy",
                    extensionClass.getName(), extensionId, e);
            throw new RuntimeException("Failed to create enhanced extension: " + extensionClass.getName(), e);
        }
    }

    /**
     * 创建增强类
     */
    @NotNull
    private <T> Class<? extends T> createEnhancedClass(@NotNull Class<T> extensionClass,
            @NotNull ClassLoader classLoader,
            @NotNull String extensionId) {
        return ByteBuddyFactory.createEnhancedClass(extensionClass, classLoader, extensionId);
    }

    /**
     * 创建扩展实例
     */
    @NotNull
    private <T> T createInstance(@NotNull Class<T> clazz, Object... args) throws Exception {
        Constructor<?> constructor = findMatchingConstructor(clazz, args);
        Object[] processedArgs = processConstructorArguments(constructor.getParameterTypes(), args);

        log.debug("Creating instance of {} with constructor: {} and {} arguments",
                clazz.getName(), constructor, processedArgs.length);

        @SuppressWarnings("unchecked")
        T instance = (T) constructor.newInstance(processedArgs);
        return instance;
    }

    /**
     * 查找匹配的构造函数
     */
    @NotNull
    private Constructor<?> findMatchingConstructor(@NotNull Class<?> clazz, Object... args)
            throws NoSuchMethodException {

        if (args == null || args.length == 0) {
            // 尝试无参构造函数
            try {
                Constructor<?> noArgCtor = clazz.getDeclaredConstructor();
                noArgCtor.setAccessible(true);
                return noArgCtor;
            } catch (NoSuchMethodException e) {
                // 继续尝试其他构造函数
            }
        }

        Class<?>[] argTypes = Arrays.stream(args)
                .map(arg -> arg != null ? arg.getClass() : Object.class)
                .toArray(Class<?>[]::new);

        log.debug("Looking for constructor in {} with {} arguments: {}",
                clazz.getName(), args != null ? args.length : 0, Arrays.toString(argTypes));

        // 策略1: 精确匹配
        Constructor<?> exactMatch = findExactMatchConstructor(clazz, argTypes);
        if (exactMatch != null) {
            return exactMatch;
        }

        // 策略2: 兼容匹配
        Constructor<?> compatibleMatch = findCompatibleConstructor(clazz, argTypes);
        if (compatibleMatch != null) {
            return compatibleMatch;
        }

        // 策略3: 参数数量匹配
        Constructor<?> countMatch = findCountMatchConstructor(clazz, args);
        if (countMatch != null) {
            return countMatch;
        }

        throw new NoSuchMethodException("No compatible constructor found for " + clazz.getName() +
                " with " + (args != null ? args.length : 0) + " arguments");
    }

    /**
     * 查找精确匹配的构造函数
     */
    @Nullable
    private Constructor<?> findExactMatchConstructor(@NotNull Class<?> clazz, @NotNull Class<?>[] argTypes) {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            Class<?>[] paramTypes = c.getParameterTypes();
            if (paramTypes.length == argTypes.length) {
                boolean exactMatch = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!paramTypes[i].isAssignableFrom(argTypes[i])) {
                        exactMatch = false;
                        break;
                    }
                }
                if (exactMatch) {
                    log.debug("Found exact constructor match: {}", c);
                    c.setAccessible(true);
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * 查找兼容的构造函数
     */
    @Nullable
    private Constructor<?> findCompatibleConstructor(@NotNull Class<?> clazz, @NotNull Class<?>[] argTypes) {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (isCompatible(c.getParameterTypes(), argTypes)) {
                log.debug("Found compatible constructor: {}", c);
                c.setAccessible(true);
                return c;
            }
        }
        return null;
    }

    /**
     * 查找参数数量匹配的构造函数
     */
    @Nullable
    private Constructor<?> findCountMatchConstructor(@NotNull Class<?> clazz, Object[] args) {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (c.getParameterCount() == (args != null ? args.length : 0)) {
                log.debug("Found constructor with matching parameter count: {}", c);
                c.setAccessible(true);
                return c;
            }
        }
        return null;
    }

    /**
     * 处理构造函数参数
     */
    @NotNull
    private Object[] processConstructorArguments(@NotNull Class<?>[] paramTypes, Object[] args) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> expected = paramTypes[i];

            // 解包包装对象
            if (arg != null && isWrapperObject(arg)) {
                arg = unwrapObject(arg);
            }

            // 类型转换
            if (arg != null) {
                arg = coerceArgument(arg, expected);
            }

            result[i] = arg;
        }
        return result;
    }

    /**
     * 检查是否为包装对象
     */
    private boolean isWrapperObject(@NotNull Object obj) {
        return obj.getClass().getSimpleName().equals("DefaultWrapper");
    }

    /**
     * 解包对象
     */
    @Nullable
    private Object unwrapObject(@NotNull Object obj) {
        try {
            Method get = obj.getClass().getMethod("get");
            Object unwrapped = get.invoke(obj);
            log.debug("Unwrapped object: {} -> {}", obj.getClass().getName(),
                    unwrapped != null ? unwrapped.getClass().getName() : "null");
            return unwrapped;
        } catch (Exception e) {
            log.debug("Failed to unwrap object: {}", e.getMessage());
            return obj;
        }
    }

    /**
     * 强制类型转换
     */
    @Nullable
    private Object coerceArgument(@NotNull Object arg, @NotNull Class<?> expectedType) {
        if (expectedType.isAssignableFrom(arg.getClass())) {
            return arg;
        }

        try {
            // 处理基本类型转换
            if (expectedType.isPrimitive()) {
                return coercePrimitive(arg, expectedType);
            }

            // 处理数字类型转换
            if (Number.class.isAssignableFrom(arg.getClass()) && Number.class.isAssignableFrom(expectedType)) {
                return coerceNumber((Number) arg, expectedType);
            }

            // 处理字符串转换
            if (expectedType == String.class) {
                return arg.toString();
            }

            // 直接转换
            return expectedType.cast(arg);
        } catch (Exception e) {
            log.warn("Failed to coerce argument from {} to {}: {}",
                    arg.getClass().getName(), expectedType.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 基本类型转换
     */
    @Nullable
    private Object coercePrimitive(@NotNull Object arg, @NotNull Class<?> expectedType) {
        if (expectedType == int.class || expectedType == Integer.class) {
            return ((Number) arg).intValue();
        } else if (expectedType == long.class || expectedType == Long.class) {
            return ((Number) arg).longValue();
        } else if (expectedType == double.class || expectedType == Double.class) {
            return ((Number) arg).doubleValue();
        } else if (expectedType == float.class || expectedType == Float.class) {
            return ((Number) arg).floatValue();
        } else if (expectedType == boolean.class || expectedType == Boolean.class) {
            return Boolean.valueOf(arg.toString());
        }
        return arg;
    }

    /**
     * 数字类型转换
     */
    @Nullable
    private Object coerceNumber(@NotNull Number number, @NotNull Class<?> expectedType) {
        if (expectedType == Integer.class || expectedType == int.class) {
            return number.intValue();
        } else if (expectedType == Long.class || expectedType == long.class) {
            return number.longValue();
        } else if (expectedType == Double.class || expectedType == double.class) {
            return number.doubleValue();
        } else if (expectedType == Float.class || expectedType == float.class) {
            return number.floatValue();
        }
        return number;
    }

    /**
     * 检查类型兼容性
     */
    private boolean isCompatible(@NotNull Class<?>[] paramTypes, @NotNull Class<?>[] argTypes) {
        if (paramTypes.length != argTypes.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> expected = paramTypes[i];
            Class<?> actual = argTypes[i];

            if (actual == null) {
                continue; // null值总是兼容的
            }

            // 处理基本类型和包装类型
            if (expected.isPrimitive()) {
                Class<?> wrapperType = getWrapperType(expected);
                if (wrapperType.isAssignableFrom(actual)) {
                    continue;
                }
            } else if (actual.isPrimitive()) {
                Class<?> actualWrapperType = getWrapperType(actual);
                if (expected.isAssignableFrom(actualWrapperType)) {
                    continue;
                }
            }

            // 直接类型检查
            if (expected.isAssignableFrom(actual)) {
                continue;
            }

            // Object类型可以接受任何类型
            if (expected == Object.class) {
                continue;
            }

            return false;
        }
        return true;
    }

    /**
     * 获取包装类型
     */
    @NotNull
    private Class<?> getWrapperType(@NotNull Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        }
        if (clazz == int.class)
            return Integer.class;
        if (clazz == long.class)
            return Long.class;
        if (clazz == boolean.class)
            return Boolean.class;
        if (clazz == double.class)
            return Double.class;
        if (clazz == float.class)
            return Float.class;
        if (clazz == char.class)
            return Character.class;
        if (clazz == byte.class)
            return Byte.class;
        if (clazz == short.class)
            return Short.class;
        return clazz;
    }

    /**
     * 创建扩展包装器
     */
    @NotNull
    @SuppressWarnings("unchecked")
    private <T> T createExtensionWrapper(@NotNull Class<T> extensionClass,
            @NotNull ClassLoader classLoader,
            @NotNull String extensionId) {
        log.debug("Creating extension wrapper for: {} with ID: {}", extensionClass.getName(), extensionId);

        return (T) new NetworkExtension() {
            @Override
            public String getId() {
                return extensionId;
            }

            @Override
            public String getName() {
                return extensionClass.getSimpleName();
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String getAuthor() {
                return "System";
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public int getStartPriority() {
                return 0;
            }

            @Override
            public ExtensionMetadata getMetadata() {
                return ExtensionMetadata.builder()
                        .id(getId())
                        .name(getName())
                        .version(getVersion())
                        .author(getAuthor())
                        .priority(getPriority())
                        .startPriority(getStartPriority())
                        .build();
            }

            @Override
            public java.nio.file.Path getExtensionFolderPath() {
                return java.nio.file.Paths.get("extensions", getId());
            }

            @Override
            public ClassLoader getExtensionClassloader() {
                return classLoader;
            }

            @Override
            public void start() throws Exception {
                log.info("Extension wrapper started: {}", getId());
            }

            @Override
            public void stop() throws Exception {
                log.info("Extension wrapper stopped: {}", getId());
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void setEnabled(boolean enabled) {
                // 包装器不支持禁用
            }

            @Override
            public boolean isStarted() {
                return true;
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public void cleanup(boolean disable) {
                log.info("Extension wrapper cleanup: {} (disable: {})", getId(), disable);
            }
        };
    }

    /**
     * 验证实例
     */
    private void validateInstance(@Nullable Object instance, @NotNull String className) {
        if (instance == null) {
            throw new RuntimeException("Constructor returned null instance for: " + className);
        }
    }

    /**
     * 验证输入参数
     */
    private void validateInput(@Nullable Object input, @NotNull String message) {
        if (input == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
