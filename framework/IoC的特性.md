先集中精力攻克 P0现代高性能 IoC 容器所必须拥有的核心能力，适用于构建类似 Spring Framework + Guice + Micronaut + AOT 的新一代企业级框架。

1. BeanDefinition：IoC 的核心元数据体系

优秀的 IoC 必须对每个 Bean 进行抽象描述，而不是简单 new。

BeanDefinition 必须包含：

Bean 类型（Class）

构造方法信息

依赖列表

Bean Scope（singleton / prototype / request / custom）

FactoryMethod / FactoryBean 支持

初始化方法 / 销毁方法

条件装配元数据

AOP 代理信息

注解元数据（AnnotationMetadata）

价值：

BeanDefinition 是 IoC 的灵魂，没有它就无法实现 AOP、自动装配、生命周期管理等高级特性。

2. 依赖注入（DI）：支持多种注入方式

支持 3 种常见注入方式：

构造器注入（推荐）

字段注入（兼容性）

Setter 注入（灵活性）

并且必须支持：

@Qualifier / @Named（多实例场景）

Optional 依赖

Lazy 依赖

Provider 模式（受 Guice 启发）

3. Bean 生命周期管理

优秀 IoC 必须具有完整生命周期：

容器生命周期：

initialize()

refresh()

close()

Bean 生命周期：

beforeCreate

afterCreate

beforeInit

afterInit

beforeDestroy

核心扩展点：

BeanPostProcessor

BeanFactoryPostProcessor

这些扩展点是 IoC 可扩展性的基础。

4. 自动扫描（Component Scanning）

IoC 必须支持从 classpath 进行自动扫描：

扫描包路径

扫描注解（@Component、@Service…）

将类转换为 BeanDefinition

将元数据注入容器

建议： 使用 ASM 而不是反射提高扫描性能。

5. 模块化系统（Module / Configuration）

参考 Google Guice 的 Module 概念，同时结合 Spring 的 @Configuration / @Bean。

模块必须支持：

手动绑定（bind）

自动绑定（scan）

有条件绑定

支持 @Import 进行模块组合

模块化 = IoC 的可扩展生态基础。

6. 条件装配（Conditional）

支持类似 Spring Boot 的条件机制：

@ConditionalOnClass

@ConditionalOnMissingBean

@ConditionalOnProperty

@ConditionalOnEnabled

自定义条件判断器

Starter 自动装配必须依赖条件装配。

7. AOP 拦截体系

具备 AOP，是优秀 IoC 的核心标志。

必须支持：

基于代理的 AOP（JDK/CGLIB/ByteBuddy）

拦截器链（InterceptorChain）

环绕/前置/后置增强

基于注解的增强（如 @Transactional）

多拦截器排序（Ordered）

用于：

事务管理

缓存

限流、熔断

日志、监控埋点

8. Scope 体系（生命周期域）

必须支持多 Scope：

singleton

prototype

request

session

thread

自定义 Scope

9. 配置绑定（Configuration Properties）

类似 Spring Boot 的强类型配置绑定：

@ConfigurationProperties(prefix="redis")
class RedisConfig {
String host;
int port;
}


必须支持：

YAML / properties

环境变量

Profile

自动绑定 + 校验

配置绑定是现代 IoC 必不可少能力。

10. 事件系统（EventBus）

内置事件驱动体系：

ApplicationEvent

事件发布/订阅

异步事件

生命周期事件（ContextStarted / ContextRefreshed / ContextClosed）

事务事件（提交前/提交后）

事件系统用于：

模块通信

容器扩展点

starter 初始化

11. AOT / 静态编译能力（未来趋势）

现代框架必须支持 “无反射运行”：

使用 APT（Annotation Processor）生成 BeanFactory 源代码

提前生成依赖图

启动时无需扫描

完美支持 GraalVM Native Image

内存占用极低

启动速度提升 90%+

Micronaut、Quarkus、Spring Boot 3 AOT 的核心思路都在这里。

12. 循环依赖检测 & 依赖图分析

IoC 必须对依赖关系进行拓扑排序（DAG 分析）：

构建依赖图

自动检测循环依赖

支持三级缓存打破循环（类似 Spring）

给出清晰错误提示

循环依赖处理是 IoC 难点，也是成熟度的体现。