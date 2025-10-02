# MQTT Protocol Extension

## 概述

MQTT (Message Queuing Telemetry Transport) 协议扩展提供了轻量级的发布/订阅消息传输机制，特别适用于IoT设备和低带宽网络环境。

## 协议信息

- **协议名称**: MQTT
- **协议版本**: 3.1.1 / 5.0
- **默认端口**: 1883 (非加密) / 8883 (TLS加密)
- **传输协议**: TCP
- **QoS级别**: 0, 1, 2

## MQTT消息格式

### 固定头部 (Fixed Header)

```
+--------+--------+--------+--------+
| Msg Type|DUP|QoS|Retain|Remaining Length|
|  (4b)  |(1b)|(2b)| (1b) |    (1-4b)     |
+--------+--------+--------+--------+
```

### 字段说明

| 字段 | 长度 | 说明 |
|------|------|------|
| Message Type | 4位 | 消息类型 (0-15) |
| DUP | 1位 | 重复标志 |
| QoS | 2位 | 服务质量等级 (0,1,2) |
| RETAIN | 1位 | 保留标志 |
| Remaining Length | 1-4字节 | 可变头部+载荷长度 |

### 消息类型

| 类型 | 值 | 方向 | 说明 |
|------|----|----|----|
| CONNECT | 1 | C→S | 客户端连接请求 |
| CONNACK | 2 | S→C | 连接确认 |
| PUBLISH | 3 | C↔S | 发布消息 |
| PUBACK | 4 | C↔S | 发布确认 (QoS 1) |
| PUBREC | 5 | C↔S | 发布收到 (QoS 2) |
| PUBREL | 6 | C↔S | 发布释放 (QoS 2) |
| PUBCOMP | 7 | C↔S | 发布完成 (QoS 2) |
| SUBSCRIBE | 8 | C→S | 订阅请求 |
| SUBACK | 9 | S→C | 订阅确认 |
| UNSUBSCRIBE | 10 | C→S | 取消订阅 |
| UNSUBACK | 11 | S→C | 取消订阅确认 |
| PINGREQ | 12 | C→S | 心跳请求 |
| PINGRESP | 13 | S→C | 心跳响应 |
| DISCONNECT | 14 | C→S | 断开连接 |

## 数据类型

### 1. CONNECT消息

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| Protocol Name | Protocol Level | Connect Flags | Keep Alive |
|    (2B)       |     (1B)       |     (1B)     |    (2B)     |
+--------+--------+--------+--------+--------+--------+--------+--------+
| Client ID Length | Client ID | ... (其他载荷) |
|      (2B)        |  (Variable) |              |
+--------+--------+--------+--------+--------+--------+--------+--------+
```

#### 连接标志位

| 位 | 标志 | 说明 |
|----|----|----|
| 7 | User Name Flag | 用户名标志 |
| 6 | Password Flag | 密码标志 |
| 5 | Will Retain | 遗嘱保留标志 |
| 4-3 | Will QoS | 遗嘱QoS |
| 2 | Will Flag | 遗嘱标志 |
| 1 | Clean Session | 清理会话标志 |
| 0 | Reserved | 保留位 |

### 2. PUBLISH消息

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| Topic Length | Topic Name | Message ID | Payload |
|    (2B)      | (Variable) |   (2B)     |(Variable)|
+--------+--------+--------+--------+--------+--------+--------+--------+
```

### 3. SUBSCRIBE消息

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| Message ID | Topic Filter | QoS | Topic Filter | QoS | ... |
|    (2B)    |  (Variable)  |(1B) |  (Variable)  |(1B) |     |
+--------+--------+--------+--------+--------+--------+--------+--------+
```

## 客户端实现

### 连接流程

1. **建立TCP连接**
   ```java
   Socket socket = new Socket("localhost", 1883);
   DataOutputStream out = new DataOutputStream(socket.getOutputStream());
   DataInputStream in = new DataInputStream(socket.getInputStream());
   ```

2. **发送CONNECT消息**
   ```java
   public void connect(String clientId, String username, String password) {
       // 构建CONNECT消息
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       DataOutputStream dos = new DataOutputStream(baos);
       
       // 固定头部
       dos.writeByte(0x10); // CONNECT消息类型
       
       // 可变头部
       writeString(dos, "MQTT"); // 协议名
       dos.writeByte(0x04);      // 协议版本
       dos.writeByte(0xC2);     // 连接标志
       dos.writeShort(60);      // Keep Alive
       
       // 载荷
       writeString(dos, clientId);
       if (username != null) writeString(dos, username);
       if (password != null) writeString(dos, password);
       
       // 发送消息
       byte[] message = baos.toByteArray();
       out.write(message);
       out.flush();
   }
   ```

3. **处理CONNACK响应**
   ```java
   public boolean waitForConnAck() throws IOException {
       byte[] response = new byte[4];
       in.readFully(response);
       
       // 检查连接结果
       return response[3] == 0x00; // 连接成功
   }
   ```

### 发布消息

```java
public void publish(String topic, String message, int qos) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    
    // 固定头部
    byte flags = (byte) (0x30 | (qos << 1)); // PUBLISH + QoS
    dos.writeByte(flags);
    
    // 可变头部
    writeString(dos, topic);
    if (qos > 0) {
        dos.writeShort(messageId++);
    }
    
    // 载荷
    dos.write(message.getBytes(StandardCharsets.UTF_8));
    
    // 发送消息
    byte[] data = baos.toByteArray();
    out.write(data);
    out.flush();
}
```

### 订阅主题

```java
public void subscribe(String topic, int qos) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    
    // 固定头部
    dos.writeByte(0x82); // SUBSCRIBE消息类型
    
    // 可变头部
    dos.writeShort(messageId++);
    
    // 载荷
    writeString(dos, topic);
    dos.writeByte(qos);
    
    // 发送消息
    byte[] data = baos.toByteArray();
    out.write(data);
    out.flush();
}
```

## 服务器端实现

### 消息处理

```java
public class MqttServer {
    private Map<String, Set<ClientSession>> subscriptions = new ConcurrentHashMap<>();
    private Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    
    public void handleMessage(byte[] data) {
        byte messageType = (byte) ((data[0] >> 4) & 0x0F);
        
        switch (messageType) {
            case 1: // CONNECT
                handleConnect(data);
                break;
            case 3: // PUBLISH
                handlePublish(data);
                break;
            case 8: // SUBSCRIBE
                handleSubscribe(data);
                break;
            case 10: // UNSUBSCRIBE
                handleUnsubscribe(data);
                break;
            case 12: // PINGREQ
                handlePingReq(data);
                break;
            case 14: // DISCONNECT
                handleDisconnect(data);
                break;
        }
    }
    
    private void handleConnect(byte[] data) {
        // 解析CONNECT消息
        String clientId = extractClientId(data);
        
        // 创建会话
        ClientSession session = new ClientSession(clientId);
        sessions.put(clientId, session);
        
        // 发送CONNACK
        sendConnAck(session);
    }
    
    private void handlePublish(byte[] data) {
        // 解析PUBLISH消息
        String topic = extractTopic(data);
        String message = extractPayload(data);
        
        // 转发给订阅者
        forwardToSubscribers(topic, message);
    }
    
    private void handleSubscribe(byte[] data) {
        // 解析SUBSCRIBE消息
        String clientId = extractClientId(data);
        String topic = extractTopic(data);
        int qos = extractQoS(data);
        
        // 添加订阅
        subscriptions.computeIfAbsent(topic, k -> new HashSet<>())
                     .add(sessions.get(clientId));
        
        // 发送SUBACK
        sendSubAck(clientId, topic, qos);
    }
}
```

## QoS级别详解

### QoS 0 - 最多一次传递

```
Client                    Server
  |                         |
  |------- PUBLISH -------->|
  |                         |
  |                         |
```

- 消息发送后不等待确认
- 可能丢失消息
- 性能最高

### QoS 1 - 至少一次传递

```
Client                    Server
  |                         |
  |------- PUBLISH -------->|
  |<------ PUBACK ----------|
  |                         |
```

- 消息发送后等待确认
- 可能重复传递
- 平衡性能和可靠性

### QoS 2 - 恰好一次传递

```
Client                    Server
  |                         |
  |------- PUBLISH -------->|
  |<------ PUBREC ----------|
  |------- PUBREL -------->|
  |<------ PUBCOMP ---------|
  |                         |
```

- 确保消息只传递一次
- 最可靠但性能最低
- 需要四次握手

## 主题和通配符

### 主题层级

```
sensor/temperature/room1
sensor/humidity/room1
device/status/camera1
```

### 通配符

| 通配符 | 说明 | 示例 |
|--------|------|------|
| `+` | 单级通配符 | `sensor/+/room1` |
| `#` | 多级通配符 | `sensor/#` |
| `$` | 系统主题 | `$SYS/broker/uptime` |

### 订阅示例

```java
// 订阅所有温度传感器
subscribe("sensor/temperature/+", 1);

// 订阅所有传感器数据
subscribe("sensor/#", 1);

// 订阅系统状态
subscribe("$SYS/broker/uptime", 0);
```

## 遗嘱消息 (Last Will)

### 遗嘱设置

```java
public void connectWithWill(String clientId, String willTopic, String willMessage) {
    // CONNECT消息中的遗嘱设置
    byte connectFlags = 0x04; // Will Flag = 1
    connectFlags |= 0x18;     // Will QoS = 1
    connectFlags |= 0x20;     // Will Retain = 1
    
    // 在载荷中添加遗嘱信息
    writeString(dos, willTopic);
    writeString(dos, willMessage);
}
```

### 遗嘱触发条件

- 客户端异常断开连接
- 网络超时
- 客户端发送DISCONNECT消息

## 安全认证

### 用户名密码认证

```java
public void connectWithAuth(String clientId, String username, String password) {
    byte connectFlags = 0xC0; // User Name Flag = 1, Password Flag = 1
    
    // 在载荷中添加认证信息
    writeString(dos, username);
    writeString(dos, password);
}
```

### TLS加密连接

```java
public void connectWithTLS(String host, int port) throws Exception {
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    
    // 启用TLS
    socket.startHandshake();
    
    // 继续MQTT协议
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    DataInputStream in = new DataInputStream(socket.getInputStream());
}
```

## 性能优化

### 连接池管理

```java
public class MqttConnectionPool {
    private final BlockingQueue<MqttClient> connections;
    private final String brokerHost;
    private final int brokerPort;
    
    public MqttClient getConnection() throws Exception {
        MqttClient client = connections.poll();
        if (client == null || !client.isConnected()) {
            client = createNewConnection();
        }
        return client;
    }
    
    public void returnConnection(MqttClient client) {
        if (client != null && client.isConnected()) {
            connections.offer(client);
        }
    }
}
```

### 消息批处理

```java
public class MqttMessageBatch {
    private final List<MqttMessage> messages = new ArrayList<>();
    private final int maxBatchSize;
    
    public void addMessage(String topic, String payload, int qos) {
        messages.add(new MqttMessage(topic, payload, qos));
        if (messages.size() >= maxBatchSize) {
            publishBatch();
        }
    }
    
    private void publishBatch() {
        for (MqttMessage message : messages) {
            publish(message.topic, message.payload, message.qos);
        }
        messages.clear();
    }
}
```

## 监控和诊断

### 连接统计

```java
public class MqttStatistics {
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalBytes = new AtomicLong(0);
    
    public void recordConnection() {
        totalConnections.incrementAndGet();
        activeConnections.incrementAndGet();
    }
    
    public void recordDisconnection() {
        activeConnections.decrementAndGet();
    }
    
    public void recordMessage(int size) {
        totalMessages.incrementAndGet();
        totalBytes.addAndGet(size);
    }
}
```

### 健康检查

```java
public class MqttHealthCheck {
    public boolean isHealthy() {
        try {
            // 发送PINGREQ
            sendPingReq();
            
            // 等待PINGRESP
            return waitForPingResp(5000);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void sendPingReq() throws IOException {
        out.writeByte(0xC0); // PINGREQ
        out.writeByte(0x00);  // 剩余长度
        out.flush();
    }
}
```

## 测试用例

### 单元测试

```java
@Test
public void testConnect() throws Exception {
    MqttClient client = new MqttClient();
    boolean connected = client.connect("test-client", null, null);
    assertTrue(connected);
    client.disconnect();
}

@Test
public void testPublishSubscribe() throws Exception {
    MqttClient publisher = new MqttClient();
    MqttClient subscriber = new MqttClient();
    
    publisher.connect("publisher", null, null);
    subscriber.connect("subscriber", null, null);
    
    subscriber.subscribe("test/topic", 1);
    publisher.publish("test/topic", "Hello MQTT", 1);
    
    String message = subscriber.receiveMessage();
    assertEquals("Hello MQTT", message);
}
```

### 压力测试

```java
@Test
public void testHighThroughput() throws Exception {
    int messageCount = 10000;
    CountDownLatch latch = new CountDownLatch(messageCount);
    
    // 创建多个发布者
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            MqttClient client = new MqttClient();
            client.connect("publisher-" + i, null, null);
            
            for (int j = 0; j < messageCount / 10; j++) {
                client.publish("test/topic", "Message " + j, 0);
                latch.countDown();
            }
        }).start();
    }
    
    assertTrue(latch.await(30, TimeUnit.SECONDS));
}
```

## 配置参数

### 服务器配置

```properties
# MQTT服务器配置
mqtt.port=1883
mqtt.tls.port=8883
mqtt.max.connections=10000
mqtt.keep.alive=60
mqtt.max.message.size=268435456
mqtt.retain.available=true
mqtt.wildcard.subscription.available=true
```

### 客户端配置

```properties
# MQTT客户端配置
mqtt.client.id=my-client
mqtt.broker.host=localhost
mqtt.broker.port=1883
mqtt.keep.alive=60
mqtt.clean.session=true
mqtt.connection.timeout=30
mqtt.automatic.reconnect=true
```

## 故障排除

### 常见问题

1. **连接失败**
   - 检查网络连接
   - 验证端口是否开放
   - 确认防火墙设置

2. **消息丢失**
   - 检查QoS级别设置
   - 验证网络稳定性
   - 调整Keep Alive时间

3. **性能问题**
   - 优化消息大小
   - 调整批处理设置
   - 使用连接池

### 调试工具

```bash
# 使用mosquitto客户端测试
mosquitto_pub -h localhost -t "test/topic" -m "Hello MQTT"
mosquitto_sub -h localhost -t "test/topic"

# 使用MQTT.fx客户端
# 下载并安装MQTT.fx进行图形化测试
```

## 版本历史

- **v1.0.0** - 支持MQTT 3.1.1协议
- **v1.1.0** - 添加TLS加密支持
- **v1.2.0** - 支持MQTT 5.0协议
- **v1.3.0** - 性能优化和监控改进
