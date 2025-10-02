# Protobuf 消息实体生成和使用指南

## 📋 概述

本指南详细说明如何通过 Protobuf 生成消息实体，包括客户端和服务器端的实现。

## 🏗️ 项目结构

```
core/
├── src/main/proto/           # Protobuf 定义文件
│   └── network_message.proto
├── src/main/java/com/dtc/core/protobuf/
│   ├── MessageFactory.java          # 消息工厂
│   ├── ClientMessageHandler.java    # 客户端消息处理器
│   ├── ServerMessageHandler.java    # 服务器端消息处理器
│   └── ProtobufMessageExample.java  # 使用示例
└── scripts/                  # 构建脚本
    ├── generate-protobuf.sh
    └── generate-protobuf.bat
```

## 🔧 环境准备

### 1. 安装 Protocol Buffers

#### Windows
```bash
# 下载并安装 protoc
# 下载地址: https://github.com/protocolbuffers/protobuf/releases
# 解压后将 bin 目录添加到 PATH 环境变量
```

#### Linux/macOS
```bash
# Ubuntu/Debian
sudo apt-get install protobuf-compiler

# macOS
brew install protobuf

# 验证安装
protoc --version
```

### 2. 配置 Maven

确保 `core/pom.xml` 中包含 Protobuf 编译插件：

```xml
<plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
    <version>0.6.1</version>
    <configuration>
        <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 🚀 生成 Java 类文件

### 方法 1: 使用脚本

#### Linux/macOS
```bash
chmod +x scripts/generate-protobuf.sh
./scripts/generate-protobuf.sh
```

#### Windows
```cmd
scripts\generate-protobuf.bat
```

### 方法 2: 使用 Maven
```bash
cd core
mvn clean compile
```

### 方法 3: 手动生成
```bash
# 生成 Java 类文件
protoc --java_out=core/src/main/java --proto_path=core/src/main/proto core/src/main/proto/network_message.proto
```

## 📝 消息类型定义

### 基础消息类型

```protobuf
enum MessageType {
    UNKNOWN = 0;
    HEARTBEAT = 1;
    DATA = 2;
    ACK = 3;
    ERROR = 4;
    CLOSE = 5;
}
```

### 网络消息结构

```protobuf
message NetworkMessage {
    string message_id = 1;
    MessageType type = 2;
    int64 timestamp = 3;
    string client_id = 4;
    string server_id = 5;
    oneof payload {
        HeartbeatMessage heartbeat = 10;
        DataMessage data = 11;
        AckMessage ack = 12;
        ErrorMessage error = 13;
        CloseMessage close = 14;
    }
}
```

## 💻 客户端使用示例

### 1. 创建消息工厂

```java
@Inject
private MessageFactory messageFactory;

@Inject
private ClientMessageHandler clientHandler;
```

### 2. 发送心跳消息

```java
String clientId = "client-001";
String serverId = "server-001";
Map<String, String> metadata = new HashMap<>();
metadata.put("version", "1.0");

NetworkMessage heartbeatMessage = clientHandler.sendHeartbeat(clientId, serverId, metadata);
```

### 3. 发送数据消息

```java
// 发送文本数据
NetworkMessage textMessage = clientHandler.sendTextData(clientId, serverId, "chat", 
                                                       "Hello, Server!", null, 1);

// 发送 JSON 数据
String jsonData = "{\"action\":\"login\",\"username\":\"user123\"}";
NetworkMessage jsonMessage = clientHandler.sendJsonData(clientId, serverId, "auth", 
                                                       jsonData, null, 2);

// 发送二进制数据
byte[] binaryData = "Hello, Binary!".getBytes();
NetworkMessage binaryMessage = messageFactory.createDataMessage(clientId, serverId, "binary", 
                                                               binaryData, "application/octet-stream", null, 1);
```

### 4. 发送业务消息

```java
// 发送用户消息
String[] roles = {"user", "admin"};
Map<String, String> attributes = new HashMap<>();
attributes.put("department", "IT");

NetworkMessage userMessage = clientHandler.sendUserMessage(clientId, serverId, 1001L, 
                                                          "john_doe", "john@example.com", 
                                                          roles, attributes);

// 发送订单消息
OrderItem[] items = {
    messageFactory.createOrderItem(1001L, "Product A", 2, 99.99),
    messageFactory.createOrderItem(1002L, "Product B", 1, 149.99)
};

NetworkMessage orderMessage = clientHandler.sendOrderMessage(clientId, serverId, 2001L, 
                                                          1001L, items, 349.97, "pending");
```

## 🖥️ 服务器端使用示例

### 1. 创建服务器处理器

```java
@Inject
private ServerMessageHandler serverHandler;
```

### 2. 处理客户端连接

```java
String clientId = "client-001";
String serverId = "server-001";

// 处理客户端连接
serverHandler.handleClientConnect(clientId, serverId);
```

### 3. 处理接收到的消息

```java
// 处理心跳消息
NetworkMessage heartbeatMessage = messageFactory.createHeartbeatMessage(clientId, serverId, null);
NetworkMessage response = serverHandler.handleReceivedMessage(heartbeatMessage);

// 处理数据消息
NetworkMessage dataMessage = messageFactory.createTextDataMessage(clientId, serverId, "chat", 
                                                                 "Hello from client!", null, 1);
NetworkMessage dataResponse = serverHandler.handleReceivedMessage(dataMessage);
```

### 4. 广播和私聊消息

```java
// 广播消息给所有客户端
NetworkMessage broadcastMessage = messageFactory.createTextDataMessage(serverId, null, "announcement", 
                                                                       "Server maintenance in 10 minutes", 
                                                                       null, 1);
serverHandler.broadcastMessage(broadcastMessage);

// 发送消息给特定客户端
NetworkMessage privateMessage = messageFactory.createTextDataMessage(serverId, null, "private", 
                                                                    "Private message for you", 
                                                                    null, 1);
serverHandler.sendMessageToClient(clientId, privateMessage);
```

## 🔄 消息序列化和反序列化

### 序列化消息

```java
// 创建消息
NetworkMessage message = messageFactory.createTextDataMessage(clientId, serverId, "test", 
                                                               "Hello, Protobuf!", null, 1);

// 序列化为字节数组
byte[] serializedData = messageFactory.serializeNetworkMessage(message);
```

### 反序列化消息

```java
// 从字节数组反序列化
NetworkMessage deserializedMessage = messageFactory.parseNetworkMessage(serializedData);
```

### 批量处理

```java
// 批量序列化
NetworkMessage[] messages = new NetworkMessage[5];
for (int i = 0; i < 5; i++) {
    messages[i] = messageFactory.createTextDataMessage(clientId, serverId, "batch", 
                                                     "Batch message " + (i + 1), null, 1);
}

// 批量序列化
byte[][] serializedMessages = new byte[messages.length][];
for (int i = 0; i < messages.length; i++) {
    serializedMessages[i] = messageFactory.serializeNetworkMessage(messages[i]);
}
```

## 📊 性能优化

### 1. 使用优化的序列化器

```java
@Inject
private OptimizedProtobufSerializer serializer;

// 预热缓存
serializer.warmupCache(NetworkMessage.class, UserMessage.class, OrderMessage.class);

// 使用优化的序列化
byte[] data = serializer.serialize(message);
NetworkMessage result = serializer.deserialize(data, NetworkMessage.class);
```

### 2. 批量处理

```java
@Inject
private BatchProtobufProcessor batchProcessor;

// 启动批量处理器
batchProcessor.start();

// 添加消息到批量队列
batchProcessor.addMessage(message);
```

### 3. 缓存管理

```java
@Inject
private SerializationCacheManager cacheManager;

// 获取缓存统计
SerializationCacheManager.CacheStats stats = cacheManager.getCacheStats();
log.info("缓存统计: {}", stats);
```

## 🧪 测试和验证

### 运行示例

```java
@Inject
private ProtobufMessageExample example;

// 运行所有演示
example.runAllDemonstrations();
```

### 性能测试

```java
@Test
public void testSerializationPerformance() {
    // 创建测试消息
    NetworkMessage message = messageFactory.createTextDataMessage("client-001", "server-001", 
                                                               "test", "Hello, World!", null, 1);
    
    // 测试序列化性能
    long startTime = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        byte[] data = messageFactory.serializeNetworkMessage(message);
        NetworkMessage result = messageFactory.parseNetworkMessage(data);
    }
    long endTime = System.nanoTime();
    
    log.info("序列化/反序列化 10000 次耗时: {}ms", (endTime - startTime) / 1_000_000);
}
```

## 🔧 配置参数

### 消息工厂配置

```properties
# 消息 ID 生成策略
protobuf.message.id.generator=uuid

# 默认消息优先级
protobuf.message.default.priority=1

# 消息超时时间 (毫秒)
protobuf.message.timeout=30000
```

### 序列化器配置

```properties
# 最大缓存大小
protobuf.cache.max.size=1000

# 缓存 TTL (毫秒)
protobuf.cache.ttl=300000

# 启用压缩
protobuf.compression.enabled=false
```

## 🚨 注意事项

### 1. 消息大小限制
- 单个消息建议不超过 1MB
- 大消息使用流式处理

### 2. 内存管理
- 定期清理缓存
- 监控内存使用情况

### 3. 错误处理
- 实现重试机制
- 记录错误日志

### 4. 版本兼容性
- 保持 Protobuf 版本一致
- 向后兼容性考虑

## 📚 参考资料

- [Protocol Buffers 官方文档](https://developers.google.com/protocol-buffers)
- [Java 序列化最佳实践](https://docs.oracle.com/javase/tutorial/jndi/objects/serial.html)
- [Netty 高性能网络编程](https://netty.io/)

## 🔄 版本历史

- **v1.0.0** - 基础消息类型定义
- **v1.1.0** - 添加业务消息类型
- **v1.2.0** - 性能优化和缓存
- **v1.3.0** - 批量处理和流式处理
