# TCP Protocol Extension

## 概述

TCP协议扩展提供了可靠的、面向连接的网络通信机制，确保数据的有序传输和完整性。该扩展适用于需要可靠数据传输的应用场景。

## 协议信息

- **协议名称**: TCP
- **协议版本**: RFC 793
- **默认端口**: 8080
- **传输协议**: TCP
- **可靠性**: 保证数据完整性和顺序

## TCP协议特性

### 连接管理

1. **三次握手建立连接**
   ```
   Client                    Server
     |                         |
     |------- SYN ------------>|
     |<------ SYN+ACK ---------|
     |------- ACK ------------>|
     |                         |
   ```

2. **四次挥手断开连接**
   ```
   Client                    Server
     |                         |
     |------- FIN ------------>|
     |<------ ACK ------------|
     |<------ FIN ------------|
     |------- ACK ------------>|
     |                         |
   ```

### 数据可靠性

- **序列号**: 确保数据顺序
- **确认号**: 保证数据接收
- **校验和**: 验证数据完整性
- **重传机制**: 处理丢包情况

## 消息格式

### 应用层消息结构

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| Length | Message Type | Timestamp |        Payload Data              |
|  (4B)  |    (1B)      |   (8B)    |        (Variable)                |
+--------+--------+--------+--------+--------+--------+--------+--------+
```

### 字段说明

| 字段 | 类型 | 长度 | 说明 |
|------|------|------|------|
| Length | int32 | 4字节 | 消息总长度（大端序） |
| Message Type | uint8 | 1字节 | 消息类型标识 |
| Timestamp | int64 | 8字节 | 时间戳（毫秒） |
| Payload Data | bytes | 变长 | 实际数据内容 |

### 消息类型

| 类型 | 值 | 说明 |
|------|----|----|
| HEARTBEAT | 0x01 | 心跳消息 |
| DATA | 0x02 | 数据消息 |
| ACK | 0x03 | 确认消息 |
| ERROR | 0x04 | 错误消息 |
| CLOSE | 0x05 | 关闭连接 |

## 数据类型

### 1. 心跳消息 (HEARTBEAT)

```java
public class HeartbeatMessage {
    private long timestamp;
    private String clientId;
    
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 消息头部
        dos.writeByte(0x01); // Message Type
        dos.writeLong(System.currentTimeMillis()); // Timestamp
        
        // 载荷
        dos.writeUTF(clientId);
        
        return baos.toByteArray();
    }
}
```

### 2. 数据消息 (DATA)

```java
public class DataMessage {
    private String data;
    private int sequenceNumber;
    private boolean requiresAck;
    
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 消息头部
        dos.writeByte(0x02); // Message Type
        dos.writeLong(System.currentTimeMillis()); // Timestamp
        
        // 载荷
        dos.writeInt(sequenceNumber);
        dos.writeBoolean(requiresAck);
        dos.writeUTF(data);
        
        return baos.toByteArray();
    }
}
```

### 3. 确认消息 (ACK)

```java
public class AckMessage {
    private int sequenceNumber;
    private boolean success;
    private String errorMessage;
    
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 消息头部
        dos.writeByte(0x03); // Message Type
        dos.writeLong(System.currentTimeMillis()); // Timestamp
        
        // 载荷
        dos.writeInt(sequenceNumber);
        dos.writeBoolean(success);
        if (!success) {
            dos.writeUTF(errorMessage);
        }
        
        return baos.toByteArray();
    }
}
```

## 客户端实现

### 连接管理

```java
public class TcpClient {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean connected = false;
    private int sequenceNumber = 0;
    
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        connected = true;
        
        // 启动心跳线程
        startHeartbeat();
    }
    
    public void disconnect() throws IOException {
        if (connected) {
            // 发送关闭消息
            sendCloseMessage();
            connected = false;
        }
        
        if (socket != null) {
            socket.close();
        }
    }
}
```

### 消息发送

```java
public void sendMessage(String data, boolean requiresAck) throws IOException {
    if (!connected) {
        throw new IllegalStateException("Not connected");
    }
    
    // 构建消息
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    
    // 消息头部
    dos.writeByte(0x02); // DATA message type
    dos.writeLong(System.currentTimeMillis());
    
    // 载荷
    dos.writeInt(sequenceNumber++);
    dos.writeBoolean(requiresAck);
    dos.writeUTF(data);
    
    byte[] message = baos.toByteArray();
    
    // 发送长度前缀
    out.writeInt(message.length);
    out.write(message);
    out.flush();
    
    if (requiresAck) {
        waitForAck(sequenceNumber - 1);
    }
}
```

### 消息接收

```java
public String receiveMessage() throws IOException {
    if (!connected) {
        throw new IllegalStateException("Not connected");
    }
    
    // 读取消息长度
    int length = in.readInt();
    if (length <= 0) {
        throw new IOException("Invalid message length: " + length);
    }
    
    // 读取消息数据
    byte[] data = new byte[length];
    in.readFully(data);
    
    // 解析消息
    return parseMessage(data);
}

private String parseMessage(byte[] data) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    
    byte messageType = dis.readByte();
    long timestamp = dis.readLong();
    
    switch (messageType) {
        case 0x01: // HEARTBEAT
            return handleHeartbeat(dis);
        case 0x02: // DATA
            return handleData(dis);
        case 0x03: // ACK
            return handleAck(dis);
        case 0x04: // ERROR
            return handleError(dis);
        case 0x05: // CLOSE
            return handleClose(dis);
        default:
            throw new IOException("Unknown message type: " + messageType);
    }
}
```

### 心跳机制

```java
private void startHeartbeat() {
    Thread heartbeatThread = new Thread(() -> {
        while (connected) {
            try {
                Thread.sleep(30000); // 30秒间隔
                sendHeartbeat();
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                log.error("Heartbeat failed", e);
                break;
            }
        }
    });
    heartbeatThread.setDaemon(true);
    heartbeatThread.start();
}

private void sendHeartbeat() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    
    dos.writeByte(0x01); // HEARTBEAT
    dos.writeLong(System.currentTimeMillis());
    dos.writeUTF("client-" + System.currentTimeMillis());
    
    byte[] message = baos.toByteArray();
    out.writeInt(message.length);
    out.write(message);
    out.flush();
}
```

## 服务器端实现

### 连接处理

```java
public class TcpServer {
    private ServerSocket serverSocket;
    private Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
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
        String clientId = "client-" + System.currentTimeMillis();
        
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            
            // 创建客户端会话
            ClientSession session = new ClientSession(clientId, clientSocket, out);
            sessions.put(clientId, session);
            
            // 处理消息循环
            while (true) {
                int length = in.readInt();
                if (length <= 0) break;
                
                byte[] data = new byte[length];
                in.readFully(data);
                
                processMessage(session, data);
            }
            
        } catch (IOException e) {
            log.error("Client connection error", e);
        } finally {
            sessions.remove(clientId);
        }
    }
}
```

### 消息处理

```java
private void processMessage(ClientSession session, byte[] data) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    
    byte messageType = dis.readByte();
    long timestamp = dis.readLong();
    
    switch (messageType) {
        case 0x01: // HEARTBEAT
            handleHeartbeat(session, dis);
            break;
        case 0x02: // DATA
            handleData(session, dis);
            break;
        case 0x05: // CLOSE
            handleClose(session, dis);
            break;
        default:
            log.warn("Unknown message type: {}", messageType);
    }
}

private void handleData(ClientSession session, DataInputStream dis) throws IOException {
    int sequenceNumber = dis.readInt();
    boolean requiresAck = dis.readBoolean();
    String data = dis.readUTF();
    
    log.info("Received data from {}: {}", session.getClientId(), data);
    
    // 处理业务逻辑
    String response = processBusinessLogic(data);
    
    // 发送响应
    if (requiresAck) {
        sendAck(session, sequenceNumber, true, null);
    }
    
    // 广播给其他客户端
    broadcastToOthers(session.getClientId(), data);
}
```

### 会话管理

```java
public class ClientSession {
    private final String clientId;
    private final Socket socket;
    private final DataOutputStream out;
    private final long connectTime;
    private final AtomicLong lastHeartbeat;
    
    public ClientSession(String clientId, Socket socket, DataOutputStream out) {
        this.clientId = clientId;
        this.socket = socket;
        this.out = out;
        this.connectTime = System.currentTimeMillis();
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
    }
    
    public void sendMessage(byte[] message) throws IOException {
        out.writeInt(message.length);
        out.write(message);
        out.flush();
    }
    
    public boolean isAlive() {
        return System.currentTimeMillis() - lastHeartbeat.get() < 60000; // 60秒超时
    }
    
    public void updateHeartbeat() {
        lastHeartbeat.set(System.currentTimeMillis());
    }
}
```

## 可靠性保证

### 重传机制

```java
public class ReliableSender {
    private final Map<Integer, PendingMessage> pendingMessages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public void sendReliableMessage(String data, int sequenceNumber) {
        PendingMessage message = new PendingMessage(data, sequenceNumber);
        pendingMessages.put(sequenceNumber, message);
        
        // 发送消息
        sendMessage(data, sequenceNumber);
        
        // 设置重传定时器
        scheduler.schedule(() -> {
            if (pendingMessages.containsKey(sequenceNumber)) {
                log.warn("Message {} not acknowledged, retransmitting", sequenceNumber);
                sendMessage(data, sequenceNumber);
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }
    
    public void handleAck(int sequenceNumber) {
        PendingMessage message = pendingMessages.remove(sequenceNumber);
        if (message != null) {
            log.debug("Message {} acknowledged", sequenceNumber);
        }
    }
}
```

### 流量控制

```java
public class FlowController {
    private final int windowSize;
    private final AtomicInteger currentWindow;
    private final BlockingQueue<Message> pendingQueue;
    
    public FlowController(int windowSize) {
        this.windowSize = windowSize;
        this.currentWindow = new AtomicInteger(0);
        this.pendingQueue = new LinkedBlockingQueue<>();
    }
    
    public boolean canSend() {
        return currentWindow.get() < windowSize;
    }
    
    public void sendMessage(Message message) {
        if (canSend()) {
            currentWindow.incrementAndGet();
            doSend(message);
        } else {
            pendingQueue.offer(message);
        }
    }
    
    public void onAck() {
        currentWindow.decrementAndGet();
        
        // 处理等待队列
        Message nextMessage = pendingQueue.poll();
        if (nextMessage != null) {
            sendMessage(nextMessage);
        }
    }
}
```

## 性能优化

### 连接池

```java
public class TcpConnectionPool {
    private final BlockingQueue<Socket> connections;
    private final String host;
    private final int port;
    private final int maxConnections;
    
    public TcpConnectionPool(String host, int port, int maxConnections) {
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.connections = new LinkedBlockingQueue<>(maxConnections);
        
        // 预创建连接
        for (int i = 0; i < maxConnections; i++) {
            try {
                Socket socket = new Socket(host, port);
                connections.offer(socket);
            } catch (IOException e) {
                log.error("Failed to create connection", e);
            }
        }
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
public class MessageBatcher {
    private final List<Message> batch = new ArrayList<>();
    private final int maxBatchSize;
    private final long maxBatchTime;
    private final ScheduledExecutorService scheduler;
    
    public MessageBatcher(int maxBatchSize, long maxBatchTime) {
        this.maxBatchSize = maxBatchSize;
        this.maxBatchTime = maxBatchTime;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // 定期发送批次
        scheduler.scheduleAtFixedRate(this::flushBatch, maxBatchTime, maxBatchTime, TimeUnit.MILLISECONDS);
    }
    
    public void addMessage(Message message) {
        synchronized (batch) {
            batch.add(message);
            if (batch.size() >= maxBatchSize) {
                flushBatch();
            }
        }
    }
    
    private void flushBatch() {
        List<Message> messagesToSend;
        synchronized (batch) {
            if (batch.isEmpty()) return;
            messagesToSend = new ArrayList<>(batch);
            batch.clear();
        }
        
        // 发送批次
        sendBatch(messagesToSend);
    }
}
```

## 监控和诊断

### 连接统计

```java
public class TcpStatistics {
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
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
    
    public void recordError() {
        errorCount.incrementAndGet();
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", totalConnections.get());
        stats.put("activeConnections", activeConnections.get());
        stats.put("totalMessages", totalMessages.get());
        stats.put("totalBytes", totalBytes.get());
        stats.put("errorCount", errorCount.get());
        return stats;
    }
}
```

### 健康检查

```java
public class TcpHealthCheck {
    private final String host;
    private final int port;
    private final int timeout;
    
    public boolean isHealthy() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public HealthStatus getDetailedStatus() {
        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), timeout);
            long responseTime = System.currentTimeMillis() - startTime;
            
            return new HealthStatus(true, responseTime, null);
        } catch (IOException e) {
            return new HealthStatus(false, -1, e.getMessage());
        }
    }
}
```

## 测试用例

### 单元测试

```java
@Test
public void testConnection() throws Exception {
    TcpClient client = new TcpClient();
    client.connect("localhost", 8080);
    assertTrue(client.isConnected());
    client.disconnect();
}

@Test
public void testMessageSendReceive() throws Exception {
    TcpClient client = new TcpClient();
    client.connect("localhost", 8080);
    
    String message = "Hello TCP";
    client.sendMessage(message, true);
    
    String response = client.receiveMessage();
    assertNotNull(response);
    
    client.disconnect();
}
```

### 压力测试

```java
@Test
public void testHighConcurrency() throws Exception {
    int clientCount = 100;
    int messagesPerClient = 1000;
    CountDownLatch latch = new CountDownLatch(clientCount);
    
    for (int i = 0; i < clientCount; i++) {
        new Thread(() -> {
            try {
                TcpClient client = new TcpClient();
                client.connect("localhost", 8080);
                
                for (int j = 0; j < messagesPerClient; j++) {
                    client.sendMessage("Message " + j, false);
                }
                
                client.disconnect();
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    assertTrue(latch.await(60, TimeUnit.SECONDS));
}
```

## 配置参数

### 服务器配置

```properties
# TCP服务器配置
tcp.port=8080
tcp.max.connections=10000
tcp.connection.timeout=30000
tcp.keep.alive=true
tcp.tcp.nodelay=true
tcp.so.linger=0
tcp.receive.buffer.size=65536
tcp.send.buffer.size=65536
```

### 客户端配置

```properties
# TCP客户端配置
tcp.server.host=localhost
tcp.server.port=8080
tcp.connection.timeout=30000
tcp.keep.alive=true
tcp.tcp.nodelay=true
tcp.retry.count=3
tcp.retry.delay=1000
```

## 故障排除

### 常见问题

1. **连接超时**
   - 检查网络连接
   - 验证端口是否开放
   - 调整连接超时设置

2. **消息丢失**
   - 检查TCP缓冲区设置
   - 验证网络稳定性
   - 启用消息确认机制

3. **性能问题**
   - 调整缓冲区大小
   - 优化消息处理逻辑
   - 使用连接池

### 调试工具

```bash
# 使用telnet测试连接
telnet localhost 8080

# 使用netcat发送数据
echo "Hello TCP" | nc localhost 8080

# 使用tcpdump抓包分析
tcpdump -i lo -A port 8080

# 使用netstat查看连接状态
netstat -an | grep 8080
```

## 版本历史

- **v1.0.0** - 基本TCP连接支持
- **v1.1.0** - 添加心跳机制
- **v1.2.0** - 实现可靠性保证
- **v1.3.0** - 性能优化和监控改进
