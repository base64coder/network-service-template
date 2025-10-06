# 注解验证指南

## 📋 概述

本项目实现了基于 `@NotNull` 和 `@Nullable` 注解的自动参数验证系统。通过 AOP（面向切面编程）技术，系统会自动拦截方法调用并验证参数和返回值。

## 🏗️ 架构组件

### 1. 注解定义
- **`@NotNull`**: 标记参数、返回值或字段不能为 null
- **`@Nullable`**: 标记参数、返回值或字段可以为 null

### 2. 核心组件
- **`AnnotationValidator`**: 注解验证器，提供验证逻辑
- **`ValidationInterceptor`**: AOP 拦截器，自动拦截方法调用
- **`ValidationModule`**: Guice 模块，配置验证拦截器

## 🚀 使用方法

### 1. 基本用法

```java
@Singleton
public class UserService {
    
    /**
     * 处理用户信息
     * @param userId 用户ID（不能为null）
     * @param username 用户名（不能为null）
     * @param email 邮箱（可以为null）
     * @return 处理结果（不能为null）
     */
    @NotNull
    public String processUser(@NotNull String userId, 
                             @NotNull String username, 
                             @Nullable String email) {
        // 方法实现
        return "User processed: " + username;
    }
}
```

### 2. 验证规则

#### 参数验证
- 标记 `@NotNull` 的参数不能为 null
- 标记 `@Nullable` 的参数可以为 null
- 未标记的参数默认可以为 null

#### 返回值验证
- 标记 `@NotNull` 的方法返回值不能为 null
- 标记 `@Nullable` 的方法返回值可以为 null
- 未标记的方法返回值默认可以为 null

### 3. 验证示例

```java
// ✅ 正确调用
userService.processUser("123", "John", "john@example.com");
userService.processUser("456", "Jane", null);

// ❌ 错误调用（会抛出 IllegalArgumentException）
userService.processUser(null, "John", "john@example.com");  // userId 不能为 null
userService.processUser("123", null, "john@example.com");    // username 不能为 null
```

## 🔧 配置

### 1. 启用验证

验证模块已自动集成到 Guice 容器中：

```java
// 在 GuiceContainerFactory 中已配置
modules.add(new ValidationModule());
```

### 2. 自定义配置

如果需要自定义验证行为，可以修改 `ValidationModule`：

```java
public class CustomValidationModule extends AbstractModule {
    @Override
    protected void configure() {
        MethodInterceptor interceptor = new ValidationInterceptor();
        
        // 只对特定包进行验证
        bindInterceptor(
            Matchers.inSubpackage("com.dtc.core.service"),
            Matchers.any(),
            interceptor
        );
    }
}
```

## 📊 验证统计

### 获取验证统计信息

```java
Method method = UserService.class.getMethod("processUser", String.class, String.class, String.class);
ValidationStats stats = AnnotationValidator.getValidationStats(method);

System.out.println("NotNull parameters: " + stats.getNotNullParameters());
System.out.println("Nullable parameters: " + stats.getNullableParameters());
System.out.println("NotNull return: " + stats.isNotNullReturn());
```

## 🛠️ 高级用法

### 1. 手动验证

```java
// 手动验证方法参数
Method method = UserService.class.getMethod("processUser", String.class, String.class, String.class);
Object[] args = {"123", "John", null};
AnnotationValidator.validateMethodParameters(method, args);

// 手动验证返回值
Object result = method.invoke(userService, args);
AnnotationValidator.validateMethodReturnValue(method, result);
```

### 2. 字段验证

```java
public class User {
    @NotNull
    private String id;
    
    @NotNull
    private String name;
    
    @Nullable
    private String email;
    
    // 验证字段值
    public void validateFields() {
        Annotation[] annotations = User.class.getDeclaredField("id").getAnnotations();
        AnnotationValidator.validateFieldValue("id", this.id, annotations);
    }
}
```

## 🚨 错误处理

### 验证失败时的异常

```java
try {
    userService.processUser(null, "John", "john@example.com");
} catch (IllegalArgumentException e) {
    log.error("Validation failed: {}", e.getMessage());
    // 输出: Validation failed: Parameter 'userId' (index 0) of method 'processUser' cannot be null
}
```

### 日志记录

验证拦截器会记录详细的日志：

```
DEBUG - Intercepting method call: UserService.processUser
DEBUG - Parameter validation passed for method: processUser
DEBUG - Method execution completed successfully: processUser
```

## 📈 性能考虑

### 1. 性能影响
- 验证拦截器会在每次方法调用时执行
- 对于高频调用的方法，可能影响性能
- 建议在生产环境中根据需要选择性启用

### 2. 优化建议
- 只对关键业务方法启用验证
- 使用包级别的拦截器配置
- 考虑在开发环境启用，生产环境禁用

## 🔍 调试和监控

### 1. 启用调试日志

```xml
<logger name="com.dtc.core.validation" level="DEBUG"/>
```

### 2. 监控验证统计

```java
// 获取验证统计
ValidationStats stats = AnnotationValidator.getValidationStats(method);
log.info("Validation stats: {}", stats);
```

## 📚 最佳实践

### 1. 注解使用原则
- 明确标记所有参数和返回值的 null 约束
- 优先使用 `@NotNull`，只在确实需要时才使用 `@Nullable`
- 保持注解的一致性

### 2. 错误处理
- 捕获 `IllegalArgumentException` 并记录详细错误信息
- 提供有意义的错误消息
- 考虑在验证失败时提供默认值或回退逻辑

### 3. 测试
- 编写单元测试验证注解行为
- 测试 null 参数和返回值的处理
- 验证错误消息的准确性

## 🎯 总结

注解验证系统提供了以下优势：

1. **自动化验证**: 无需手动编写 null 检查代码
2. **类型安全**: 编译时和运行时的一致性检查
3. **文档化**: 注解本身就是很好的文档
4. **可配置**: 可以根据需要启用或禁用验证
5. **性能监控**: 提供详细的验证统计信息

通过合理使用 `@NotNull` 和 `@Nullable` 注解，可以大大提高代码的健壮性和可维护性。
