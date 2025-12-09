# 分布式模块 (Distributed Module)

本模块为网络服务框架提供原生的分布式能力，基于 SOFAJRaft (Raft 一致性算法) 实现，支持服务注册发现、RPC 通信等核心功能。

## 🌟 核心特性

- **Raft 一致性**: 基于 JRaft 实现强一致性的元数据管理和服务注册表。
- **服务自动注册**: 通过 `@RpcService` 注解自动扫描并注册服务到集群。
- **服务自动发现**: 通过 `@RpcReference` 注解自动注入远程服务代理，支持负载均衡。
- **高性能 RPC**: 内置基于 Netty + Protobuf 的高性能 RPC 通信层。
- **无缝集成**: 通过 SPI 和 StartupHook 机制与 Core 框架无缝集成，开箱即用。

## 🚀 快速开始

### 1. 启用分布式功能

分布式模块默认集成在框架中。只需在启动时通过系统属性配置集群信息即可。

### 2. 定义 RPC 服务接口

```java
public interface CalculatorService {
    int add(int a, int b);
}
```

### 3. 实现并暴露服务 (服务端)

使用 `@RpcService` 注解标记实现类：

```java
import com.dtc.api.rpc.RpcService;
import javax.inject.Singleton;

@Singleton
@RpcService(name = "calculator", version = "1.0.0")
public class CalculatorServiceImpl implements CalculatorService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
```

### 4. 调用远程服务 (客户端)

使用 `@RpcReference` 注解注入代理：

```java
import com.dtc.api.rpc.RpcReference;
import javax.inject.Inject;

public class MyController {
    
    @RpcReference(name = "calculator", timeout = 5000)
    private CalculatorService calculatorService;
    
    public void doWork() {
        int result = calculatorService.add(10, 20);
        System.out.println("Result: " + result);
    }
}
```

## ⚙️ 配置说明

可以通过 Java 系统属性 (`-D`) 或环境变量来配置分布式模块。

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `cluster.registry.type` | `raft` | 注册中心类型，目前支持 `raft` |
| `cluster.registry.address` | `127.0.0.1:8888` | 当前节点的 Raft 内部通信地址 (IP:Port) |
| `cluster.registry.group` | `default_group` | Raft 组名称，同一集群内的节点必须一致 |
| `cluster.rpc.port` | `8090` | RPC 服务监听端口，用于接收远程调用 |
| `cluster.data.path` | `raft_data` | Raft 日志和快照数据的存储路径 |
| `cluster.registry.initConf` | (同 address) | **仅首个节点需要**。集群初始节点列表，格式 `ip:port,ip:port` |

### 启动示例

**节点 1 (Leader):**
```bash
java -Dcluster.registry.address=127.0.0.1:8881 \
     -Dcluster.rpc.port=8091 \
     -Dcluster.data.path=./data/node1 \
     -Dcluster.registry.initConf=127.0.0.1:8881,127.0.0.1:8882,127.0.0.1:8883 \
     -jar network-service.jar
```

**节点 2:**
```bash
java -Dcluster.registry.address=127.0.0.1:8882 \
     -Dcluster.rpc.port=8092 \
     -Dcluster.data.path=./data/node2 \
     -Dcluster.registry.initConf=127.0.0.1:8881,127.0.0.1:8882,127.0.0.1:8883 \
     -jar network-service.jar
```

**节点 3:**
```bash
java -Dcluster.registry.address=127.0.0.1:8883 \
     -Dcluster.rpc.port=8093 \
     -Dcluster.data.path=./data/node3 \
     -Dcluster.registry.initConf=127.0.0.1:8881,127.0.0.1:8882,127.0.0.1:8883 \
     -jar network-service.jar
```

## 🏗️ 架构设计

### 模块结构

- **registry**: 包含 `ServiceRegistry` 接口和基于 Raft 的 `RaftServiceRegistry` 实现。
- **rpc**: 包含 RPC Server/Client、Protobuf 协议定义及动态代理逻辑。
- **manager**: `ClusterManager` 负责协调服务启动、注册和销毁。

### 交互流程

1. **启动**: `NetworkService` 启动 -> `DistributedModule` 加载 -> `ClusterManager` 启动。
2. **注册**: `ClusterManager` 扫描 `@RpcService` Bean -> 调用 `RaftServiceRegistry` -> 写入 Raft Log -> 状态机更新。
3. **发现**: `RpcReferenceBeanPostProcessor` 扫描字段 -> 创建动态代理。
4. **调用**: 代理拦截方法调用 -> 查询 `ServiceDiscovery` 获取地址 -> `RpcClient` 发送 Protobuf 请求 -> `RpcServer` 处理并返回。

