# Network Service Template

基于 Netty 和 LMAX Disruptor 的高性能、模块化网络服务框架模板。它不仅支持网络协议的热插拔扩展，还内置了强大的持久化层和分布式事务支持，旨在为开发者提供一个低延迟、高吞吐、易扩展的下一代网络应用基座。

## 🚀 项目特性

- **⚡ 极致性能**
    - **Netty 驱动** - 基于 Netty 4.x 的异步非阻塞 IO 模型。
    - **Disruptor 核心** - 引入 [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/) 无锁环形队列作为核心消息总线，实现纳秒级消息分发。
    - **零拷贝** - 优化的缓冲区管理和高效的 Protobuf 序列化支持。

- **💾 强大的持久化层 (Robust Persistence)**
    - **多方言支持** - 内置 MySQL, PostgreSQL, H2 等多种数据库方言适配 (`Dialect`)。
    - **读写分离** - 支持主从 (`Master-Slave`) 多数据源智能路由，通过 `@Transactional(readOnly=true)` 自动切换。
    - **分布式事务** - 内置轻量级分布式事务管理器，支持跨服务的全局事务协调 (`@DistributedTransactional`)。

- **🏗️ 现代化的 IoC 容器**
    - **轻量级注入** - 实现了参考 Guice/Spring 的轻量级依赖注入容器。
    - **全功能 AOP** - 支持切面编程，轻松实现日志、事务、权限控制。
    - **生命周期管理** - 完善的 Bean 生命周期和应用事件机制。

- **🌐 全栈协议支持**
    - **多协议接入** - 内置 HTTP/HTTPS, WebSocket, TCP, MQTT, UDP 支持。
    - **热插拔扩展** - 协议层采用 SPI 设计，支持运行时动态加载/卸载协议扩展。

- **🛠️ 开发者友好**
    - **注解驱动** - 使用 `@MessageHandler`, `@Inject`, `@Repository`, `@Transactional` 等注解简化开发。
    - **模块化设计** - 清晰的 `api`, `core`, `framework` 分层架构。
    - **完备的监控** - 内置指标收集 (`Metrics`)、健康检查 (`HealthCheck`) 和系统诊断。

## 📁 项目结构

```
network-service-template/
├── api/                             # 扩展 API 接口定义
├── core/                            # 核心框架实现 (Netty, Disruptor, Persistence)
├── framework/                       # 基础框架层
│   ├── annotations/                 # 核心注解定义
│   ├── beans/                       # 自研 IoC/AOP 容器实现
├── extensions/                      # 协议扩展示例
│   ├── mqtt-extension/              # MQTT 协议实现
│   ├── websocket-extension/         # WebSocket 协议实现
│   └── tcp-extension/               # TCP 协议实现
└── distribution/                    # 发行包构建
```

## 🏗️ 核心架构

### 系统分层

```mermaid
graph TD
    User[用户业务代码] --> Extension[扩展层 (API/SPI)]
    Extension --> Framework[框架层 (IoC, AOP, Utils)]
    Framework --> Persistence[持久化层 (JDBC, TX, Dialect)]
    Persistence --> Core[核心层 (Netty, Disruptor, Metrics)]
```

### 消息处理流水线

采用了 Reactor + Disruptor 模式，将 IO 线程与业务线程彻底解耦：

1.  **Netty IO Threads**: 负责连接接入、协议编解码 (Codec)。
2.  **Disruptor RingBuffer**: 无锁高性能队列，缓冲并通过内存屏障传递消息。
3.  **Worker Threads**: 消费者从队列获取消息，路由到具体的 `@MessageHandler` 执行业务逻辑。

## 🚀 快速开始

### 1. 环境要求
*   JDK 17+ (推荐 21)
*   Maven 3.8+

### 2. 构建项目

```bash
# 克隆项目
git clone <repository-url>
cd network-service-template

# 构建并运行测试
mvn clean install
```

### 3. 运行服务

```bash
# 进入发行包目录
cd distribution/target/network-service-1.0.0

# 启动服务
./bin/start.sh
```

## 🔧 开发指南

### 1. 声明持久化 Repository
框架提供了类似 Spring Data 的 `JdbcRepository`：

```java
// 1. 定义实体
@Table(name = "users")
public class User {
    @Id(keyType = KeyType.AUTO)
    private Long id;
    private String username;
}

// 2. 声明 Repository
@Repository
public class UserRepository extends JdbcRepository<User, Long> {
    @Inject
    public UserRepository(DataSourceProvider provider) { super(provider); }
}

// 3. 使用事务 (支持读写分离)
@Service
public class UserService {
    @Inject UserRepository userRepo;

    @Transactional(readOnly = false) // 自动路由到主库，并在异常时回滚
    public void createUser(User user) {
        userRepo.save(user);
    }
    
    @DistributedTransactional // 开启分布式全局事务
    public void createGlobalUser(User user) {
        // ...
    }
}
```

### 2. 自定义协议扩展

实现一个自定义协议只需三步：

1.  **定义协议**: 实现 `ProtocolExtension` 接口。
2.  **编写处理器**: 使用注解标记业务逻辑。
3.  **注册扩展**: 通过 SPI 或 IoC 容器注册。

## 📊 监控和观测

框架内置了全面的观测指标：

*   **JVM 指标**: 内存、GC、线程状态。
*   **网络指标**: 连接数、吞吐量、包大小分布。
*   **Disruptor 指标**: 队列深度、消费延迟、生产速率。

可以通过 `DiagnosticService` 或暴露的 HTTP 端点获取。

## 💡 项目现状与设计验证

### 核心功能实现状态

| 设计目标 | 实现状态 | 具体实现分析 |
| :--- | :--- | :--- |
| **基于 Netty** | ✅ 已实现 | 核心通信层 (`core/network/netty`) 封装了 Netty，提供了健壮的 NIO 通信基座。 |
| **高性能 (High Perf)** | ✅ 超预期 | 引入 **LMAX Disruptor** (`core/queue/DisruptorQueue`) 作为核心消息总线，实现了 IO 线程与业务线程的无锁解耦。 |
| **持久化与事务** | ✅ 新增 | 实现了多方言 JDBC 封装、**读写分离** (`RoutingDataSource`) 和 **分布式事务** (`GlobalTransactionManager`)。 |
| **模块化/热插拔** | ✅ 已实现 | 工程结构清晰，实现了轻量级 IoC 容器和模块化加载机制。 |
| **多协议支持** | ✅ 已实现 | 内置 HTTP, TCP, WebSocket, MQTT, UDP 及 Custom 协议支持。 |

---
*Built with ❤️ for high-performance network applications.*
