# IoC 容器架构评估与演进规划

## 1. 当前 IOC 实现分析与评估

目前框架已经具备了 IoC 容器的雏形，正在从纯 Guice 模式向 "Spring 风格定义 + Guice 风格配置" 的混合模式转型。

| 维度 | 当前状态 | 评估 | 改进方向 |
| :--- | :--- | :--- | :--- |
| **配置方式** | 混合模式：支持 `NetModule` (Guice) 和注解扫描 (Spring)。 | **可用**。兼容性做得不错，但两者边界需要更清晰。 | 统一底层为 `BeanDefinition`。Module 的 `configure()` 方法本质上是在编程式地注册 BeanDefinition。 |
| **Bean 定义** | `BeanDefinition` 接口已引入。 | **基础**。属性还比较少（缺少 lazy, primary, qualifiers 等）。 | 完善 `BeanDefinition`，使其成为容器的唯一真理来源。 |
| **生命周期** | 有 `PostConstruct`, `PreDestroy`，`ApplicationListener`。 | **良好**。基本能满足需求。 | 引入更细粒度的阶段：`BeanPostProcessor` (Before/After init), `InstantiationAwareBeanPostProcessor`。 |
| **依赖注入** | 主要靠反射 (`field.set`, `method.invoke`)。 | **性能瓶颈**。大量使用反射会拖慢启动速度。 | **引入 ByteBuddy** 生成访问器，或 **AOT** 生成工厂代码。 |
| **AOP** | 有 `aop-aspects` 模块，但尚未深度集成。 | **待完善**。需要通过 `BeanPostProcessor` 自动代理。 | 使用 **ByteBuddy** 动态生成子类代理，替代 JDK Proxy/CGLIB。 |
| **条件加载** | `Condition` 接口已引入。 | **可用**。 | 增强 `ConditionContext`，支持读取 Environment 和其他 Bean 定义。 |
| **AOT (Dagger)** | 尚未实现。目前依赖运行时扫描 (`BeanScanner`)。 | **缺失**。这是高性能的关键。 | 实现 `Annotation Processor`，在编译期生成 `BeanDefinition` 注册代码。 |

---

## 2. 演进设计方案：融合三者优势

我们将构建一个 **"编译时辅助、运行时增强"** 的 IoC 容器，命名为 **Net-IoC**。

### 核心架构图

```mermaid
graph TD
    UserCode[用户代码] -->|@Inject / @Component| AOT_Processor[AOT Annotation Processor]
    UserCode -->|NetModule| Runtime_Config[运行时配置]
    
    subgraph Compile_Time [编译期 (Dagger风格)]
        AOT_Processor -->|生成| BeanFactory_Source[Bean工厂源码]
        AOT_Processor -->|生成| Injector_Source[注入器源码]
    end
    
    subgraph Runtime [运行期 (Spring风格)]
        BeanFactory_Source -->|加载| BeanDefinition_Map[Bean定义注册表]
        Runtime_Config -->|编程式注册| BeanDefinition_Map
        
        BeanDefinition_Map -->|实例化| Bean_Instance[Bean实例]
        
        Bean_Instance -->|BeanPostProcessor| AOP_Proxy[AOP代理 (ByteBuddy)]
        
        Bean_Instance -->|Lifecycle| Init_Methods[初始化方法]
    end
```

### 关键技术点实现策略

#### 1. 统一 BeanDefinition (Spring 核心)
无论是由 `NetModule` (Guice) 配置进来的，还是 `@Component` (Spring) 扫描进来的，最终都转化为 `BeanDefinition`。
*   **改进**：确保 `Binder` (Guice兼容层) 在 `install()` 时，不是直接创建对象，而是创建 `BeanDefinition`。

#### 2. 引入 ByteBuddy 替代反射 (高性能运行时)
不要直接使用 `method.invoke` 或 `field.set`。
*   **依赖注入**：对于 private 字段，使用 ByteBuddy 生成 `Accessor` 类，或者在 AOT 阶段生成反射调用的替代代码。
*   **AOP**：实现一个 `AspectJAutoProxyBeanPostProcessor`。当发现 Bean 需要 AOP 增强时，使用 ByteBuddy 动态创建该 Bean 的子类，并拦截方法调用。

#### 3. 实现 AOT Annotation Processor (Dagger 核心)
创建一个新的模块 `framework/ioc-aot`。
*   **目标**：替代 `BeanScanner` (运行时扫描 classpath 非常慢)。
*   **实现**：使用 `javax.annotation.processing.Processor`。
*   **产物**：为每个模块生成一个 `Module_BeanRegistrar` 类。
    ```java
    // 自动生成的代码示例
    public class UserModule_Registrar implements BeanRegistrar {
        public void register(BeanDefinitionRegistry registry) {
            registry.registerBeanDefinition("userService", new RootBeanDefinition(UserService.class));
        }
    }
    ```
*   启动时，`NetApplicationContext` 只需要加载这些生成的 Registrar 类，速度极快。

#### 4. 增强的模块化 (Guice 核心)
保留 `NetModule`，但赋予它 Spring `@Configuration` 的能力。
*   支持在 Module 中使用 `@Provides` 注解定义 Bean（Guice 风格）。
*   支持 `Condition` 条件判断（Spring `@Conditional`）。

---

## 3. 下一步执行计划

我们已经完成了基础的重命名和兼容性修复。现在的首要任务是让代码**编译通过**，然后逐步引入 AOT 和 ByteBuddy。

1.  **Phase 1: 巩固核心 (Current)**
    *   确保当前的 `NetApplicationContext` 和 `BeanDefinitionReader` 稳定。
    *   修复现有的编译错误（特别是 `AbstractModule` 和 `BeanDefinitionReader` 的遗留问题）。
    *   确保 Guice 兼容层能跑通。

2.  **Phase 2: 引入 ByteBuddy 实现 AOP**
    *   在 `framework/aop` 中引入 ByteBuddy 依赖。
    *   创建一个 `ByteBuddyAopProxyGenerator`，替换原有的代理逻辑。
    *   实现基于注解的切面拦截。

3.  **Phase 3: Dagger 风格的 AOT 实现**
    *   创建 `framework/ioc-annotation-processor` 模块。
    *   实现注解处理器，生成 `BeanDefinition` 注册代码和 Bean 工厂代码。

