# Custom Protocol Extension

## 概述

自定义协议扩展提供了灵活的消息传输机制，支持自定义编解码器和消息处理逻辑。该扩展适用于需要特殊协议格式的应用场景。

## 协议信息

- **协议名称**: CustomProtocol
- **协议版本**: 1.0.0
- **默认端口**: 9999
- **传输协议**: TCP
- **编码格式**: UTF-8

## 消息格式

### 消息结构

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| Length |                    Message Data                           |
|  (4B)  |                    (Variable)                             |
+--------+--------+--------+--------+--------+--------+--------+--------+
```

### 字段说明

| 字段 | 类型 | 长度 | 说明 |
|------|------|------|------|
| Length | int32 | 4字节 | 消息数据长度（大端序） |
| Message Data | string | 变长 | UTF-8编码的消息内容 |

### 消息示例

#### 客户端发送消息
```
Length: 0x0000000D (13字节)
Data: "Hello Server"
```

#### 服务器响应消息
```
Length: 0x0000000C (12字节)
Data: "Hello Client"
```

## 数据类型

### 支持的消息类型

1. **文本消息** (String)
   - 编码: UTF-8
   - 最大长度: 64KB
   - 示例: "Hello World"

2. **JSON消息** (JSON String)
   - 编码: UTF-8
   - 格式: 标准JSON
   - 示例: `{"type":"message","content":"Hello","timestamp":1234567890}`

3. **二进制消息** (Byte Array)
   - 编码: 原始字节
   - 最大长度: 1MB
   - 示例: `[0x48, 0x65, 0x6C, 0x6C, 0x6F]`

## 客户端实现

### 连接流程

1. **建立TCP连接**
   ```java
   Socket socket = new Socket("localhost", 9999);
   DataOutputStream out = new DataOutputStream(socket.getOutputStream());
   DataInputStream in = new DataInputStream(socket.getInputStream());
   ```

2. **发送消息**
   ```java
   String message = "Hello Server";
   byte[] data = message.getBytes(StandardCharsets.UTF_8);
   out.writeInt(data.length);  // 写入长度
   out.write(data);            // 写入数据
   out.flush();
   ```

3. **接收消息**
   ```java
   int length = in.readInt();   // 读取长度
   byte[] data = new byte[length];
   in.readFully(data);         // 读取数据
   String response = new String(data, StandardCharsets.UTF_8);
   ```

### 客户端示例代码

```java
public class CustomProtocolClient {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }
    
    public void sendMessage(String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }
    
    public String receiveMessage() throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return new String(data, StandardCharsets.UTF_8);
    }
    
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}
```

## 服务器端实现

### 消息处理流程

1. **接收连接**
   - 客户端连接到端口9999
   - 服务器创建新的处理线程

2. **消息解码**
   - 读取4字节长度字段
   - 根据长度读取消息数据
   - 将字节数据转换为字符串

3. **消息处理**
   - 调用协议扩展的 `onMessage()` 方法
   - 执行自定义业务逻辑
   - 生成响应消息

4. **消息编码**
   - 将响应转换为字节数组
   - 写入4字节长度字段
   - 写入消息数据

### 服务器端示例代码

```java
public class CustomProtocolServer {
    private ServerSocket serverSocket;
    private boolean running = false;
    
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        while (running) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            
            while (true) {
                // 读取消息长度
                int length = in.readInt();
                if (length <= 0) break;
                
                // 读取消息数据
                byte[] data = new byte[length];
                in.readFully(data);
                String message = new String(data, StandardCharsets.UTF_8);
                
                // 处理消息
                String response = processMessage(message);
                
                // 发送响应
                byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                out.writeInt(responseData.length);
                out.write(responseData);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String processMessage(String message) {
        // 自定义消息处理逻辑
        return "Echo: " + message;
    }
}
```

## 错误处理

### 常见错误码

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| -1 | 消息长度无效 | 检查长度字段值 |
| -2 | 消息数据不完整 | 重新发送消息 |
| -3 | 编码格式错误 | 检查UTF-8编码 |
| -4 | 消息过大 | 分割消息 |

### 错误处理示例

```java
public void handleMessage(byte[] data) {
    try {
        // 检查消息长度
        if (data.length < 4) {
            throw new ProtocolException("Invalid message length");
        }
        
        // 解析长度字段
        int length = ByteBuffer.wrap(data, 0, 4).getInt();
        if (length <= 0 || length > MAX_MESSAGE_SIZE) {
            throw new ProtocolException("Invalid message size: " + length);
        }
        
        // 检查数据完整性
        if (data.length < 4 + length) {
            throw new ProtocolException("Incomplete message data");
        }
        
        // 提取消息内容
        String message = new String(data, 4, length, StandardCharsets.UTF_8);
        processMessage(message);
        
    } catch (Exception e) {
        log.error("Failed to process message", e);
        sendErrorResponse(e.getMessage());
    }
}
```

## 性能优化

### 连接池配置

```java
// 客户端连接池
public class ConnectionPool {
    private final BlockingQueue<Socket> connections;
    private final String host;
    private final int port;
    private final int maxConnections;
    
    public ConnectionPool(String host, int port, int maxConnections) {
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.connections = new LinkedBlockingQueue<>(maxConnections);
    }
    
    public Socket getConnection() throws IOException {
        Socket socket = connections.poll();
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, port);
        }
        return socket;
    }
    
    public void returnConnection(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            connections.offer(socket);
        }
    }
}
```

### 消息批处理

```java
public class MessageBatch {
    private final List<String> messages = new ArrayList<>();
    private final int maxBatchSize;
    
    public void addMessage(String message) {
        messages.add(message);
        if (messages.size() >= maxBatchSize) {
            sendBatch();
        }
    }
    
    private void sendBatch() {
        // 批量发送消息
        String batchData = String.join("\n", messages);
        sendMessage(batchData);
        messages.clear();
    }
}
```

## 测试用例

### 单元测试

```java
@Test
public void testMessageEncoding() {
    String message = "Hello World";
    byte[] encoded = encodeMessage(message);
    String decoded = decodeMessage(encoded);
    assertEquals(message, decoded);
}

@Test
public void testLargeMessage() {
    String largeMessage = "A".repeat(10000);
    byte[] encoded = encodeMessage(largeMessage);
    String decoded = decodeMessage(encoded);
    assertEquals(largeMessage, decoded);
}

@Test
public void testInvalidMessage() {
    byte[] invalidData = {0x00, 0x00, 0x00, 0x01}; // 长度1但无数据
    assertThrows(ProtocolException.class, () -> decodeMessage(invalidData));
}
```

### 集成测试

```java
@Test
public void testClientServerCommunication() throws Exception {
    // 启动服务器
    CustomProtocolServer server = new CustomProtocolServer();
    server.start(9999);
    
    // 连接客户端
    CustomProtocolClient client = new CustomProtocolClient();
    client.connect("localhost", 9999);
    
    // 发送消息
    client.sendMessage("Hello Server");
    String response = client.receiveMessage();
    
    // 验证响应
    assertEquals("Echo: Hello Server", response);
    
    // 清理资源
    client.close();
    server.stop();
}
```

## 配置参数

### 扩展配置

```xml
<configuration>
    <property name="enabled" value="true"/>
    <property name="port" value="9999"/>
    <property name="protocol-name" value="CustomProtocol"/>
    <property name="max-message-size" value="1048576"/>
    <property name="connection-timeout" value="30000"/>
    <property name="keep-alive" value="true"/>
</configuration>
```

### 服务器配置

```properties
# 自定义协议配置
custom.protocol.port=9999
custom.protocol.max.connections=1000
custom.protocol.message.timeout=30000
custom.protocol.buffer.size=8192
```

## 监控和日志

### 日志配置

```xml
<logger name="com.dtc.custom" level="DEBUG">
    <appender-ref ref="CUSTOM_PROTOCOL_FILE"/>
</logger>
```

### 监控指标

- 连接数统计
- 消息吞吐量
- 错误率统计
- 响应时间分布

## 故障排除

### 常见问题

1. **连接超时**
   - 检查网络连接
   - 验证端口是否开放
   - 确认防火墙设置

2. **消息乱码**
   - 检查字符编码设置
   - 验证UTF-8编码
   - 确认消息格式

3. **性能问题**
   - 调整缓冲区大小
   - 优化消息处理逻辑
   - 使用连接池

### 调试工具

```bash
# 使用telnet测试连接
telnet localhost 9999

# 使用netcat发送消息
echo "Hello Server" | nc localhost 9999

# 使用tcpdump抓包分析
tcpdump -i lo -A port 9999
```

## 版本历史

- **v1.0.0** - 初始版本，支持基本的文本消息传输
- **v1.1.0** - 添加JSON消息支持
- **v1.2.0** - 添加二进制消息支持
- **v1.3.0** - 性能优化和错误处理改进
