# WebSocket Protocol Extension

## 概述

WebSocket协议扩展提供了全双工、低延迟的实时通信机制，特别适用于Web应用和实时数据推送场景。该扩展基于RFC 6455标准实现。

## 协议信息

- **协议名称**: WebSocket
- **协议版本**: RFC 6455
- **默认端口**: 8081
- **传输协议**: TCP over HTTP
- **握手协议**: HTTP Upgrade

## WebSocket协议特性

### 连接建立

1. **HTTP握手**
   ```
   Client Request:
   GET /chat HTTP/1.1
   Host: localhost:8081
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
   Sec-WebSocket-Version: 13
   
   Server Response:
   HTTP/1.1 101 Switching Protocols
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
   ```

2. **协议升级**
   - 从HTTP协议升级到WebSocket协议
   - 保持TCP连接，改变应用层协议

### 数据帧格式

```
+--------+--------+--------+--------+--------+--------+--------+--------+
| FIN | RSV | Opcode | Mask | Payload Len | Masking Key | Payload Data |
|(1b) |(3b) |  (4b)  |(1b)  |   (7b)      |   (4B)      |  (Variable)  |
+--------+--------+--------+--------+--------+--------+--------+--------+
```

### 字段说明

| 字段 | 长度 | 说明 |
|------|------|------|
| FIN | 1位 | 帧结束标志 |
| RSV | 3位 | 保留位，必须为0 |
| Opcode | 4位 | 操作码 |
| Mask | 1位 | 掩码标志 |
| Payload Len | 7位 | 载荷长度 |
| Masking Key | 4字节 | 掩码密钥（客户端必须设置） |
| Payload Data | 变长 | 实际数据 |

### 操作码 (Opcode)

| 值 | 名称 | 说明 |
|----|----|----|
| 0x0 | Continuation | 继续帧 |
| 0x1 | Text | 文本帧 |
| 0x2 | Binary | 二进制帧 |
| 0x8 | Close | 关闭帧 |
| 0x9 | Ping | Ping帧 |
| 0xA | Pong | Pong帧 |

## 消息格式

### 文本消息

```java
public class TextMessage {
    private String content;
    private long timestamp;
    private String messageId;
    
    public byte[] toWebSocketFrame() {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        return createFrame(0x81, data); // FIN=1, Opcode=1 (Text)
    }
    
    private byte[] createFrame(int opcode, byte[] payload) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 帧头部
        baos.write(opcode); // FIN + Opcode
        
        // 载荷长度
        if (payload.length < 126) {
            baos.write(payload.length);
        } else if (payload.length < 65536) {
            baos.write(126);
            baos.write((payload.length >> 8) & 0xFF);
            baos.write(payload.length & 0xFF);
        } else {
            baos.write(127);
            for (int i = 7; i >= 0; i--) {
                baos.write((int) ((payload.length >> (i * 8)) & 0xFF));
            }
        }
        
        // 载荷数据
        baos.write(payload, 0, payload.length);
        
        return baos.toByteArray();
    }
}
```

### 二进制消息

```java
public class BinaryMessage {
    private byte[] data;
    private String contentType;
    
    public byte[] toWebSocketFrame() {
        return createFrame(0x82, data); // FIN=1, Opcode=2 (Binary)
    }
    
    public static BinaryMessage fromImage(byte[] imageData) {
        BinaryMessage message = new BinaryMessage();
        message.data = imageData;
        message.contentType = "image/jpeg";
        return message;
    }
    
    public static BinaryMessage fromJson(String json) {
        BinaryMessage message = new BinaryMessage();
        message.data = json.getBytes(StandardCharsets.UTF_8);
        message.contentType = "application/json";
        return message;
    }
}
```

### 控制消息

```java
public class ControlMessage {
    // Ping消息
    public static byte[] createPing(byte[] payload) {
        return createFrame(0x89, payload); // FIN=1, Opcode=9 (Ping)
    }
    
    // Pong消息
    public static byte[] createPong(byte[] payload) {
        return createFrame(0x8A, payload); // FIN=1, Opcode=10 (Pong)
    }
    
    // Close消息
    public static byte[] createClose(int code, String reason) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write((code >> 8) & 0xFF);
        baos.write(code & 0xFF);
        if (reason != null) {
            baos.write(reason.getBytes(StandardCharsets.UTF_8));
        }
        return createFrame(0x88, baos.toByteArray()); // FIN=1, Opcode=8 (Close)
    }
}
```

## 客户端实现

### 连接建立

```java
public class WebSocketClient {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean connected = false;
    private String secWebSocketKey;
    
    public void connect(String host, int port, String path) throws IOException {
        // 建立TCP连接
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        
        // 发送HTTP握手请求
        sendHandshakeRequest(host, port, path);
        
        // 处理握手响应
        if (handleHandshakeResponse()) {
            connected = true;
            startMessageLoop();
        } else {
            throw new IOException("WebSocket handshake failed");
        }
    }
    
    private void sendHandshakeRequest(String host, int port, String path) throws IOException {
        secWebSocketKey = generateWebSocketKey();
        
        String request = String.format(
            "GET %s HTTP/1.1\r\n" +
            "Host: %s:%d\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Key: %s\r\n" +
            "Sec-WebSocket-Version: 13\r\n" +
            "\r\n",
            path, host, port, secWebSocketKey
        );
        
        out.write(request.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
    
    private boolean handleHandshakeResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        
        // 读取HTTP响应头
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            response.append(line).append("\r\n");
        }
        
        // 检查升级响应
        return response.toString().contains("101 Switching Protocols") &&
               response.toString().contains("Sec-WebSocket-Accept");
    }
}
```

### 消息发送

```java
public void sendTextMessage(String message) throws IOException {
    if (!connected) {
        throw new IllegalStateException("Not connected");
    }
    
    byte[] data = message.getBytes(StandardCharsets.UTF_8);
    byte[] frame = createTextFrame(data);
    out.write(frame);
    out.flush();
}

public void sendBinaryMessage(byte[] data) throws IOException {
    if (!connected) {
        throw new IllegalStateException("Not connected");
    }
    
    byte[] frame = createBinaryFrame(data);
    out.write(frame);
    out.flush();
}

private byte[] createTextFrame(byte[] data) {
    return createFrame(0x81, data); // FIN=1, Opcode=1 (Text)
}

private byte[] createBinaryFrame(byte[] data) {
    return createFrame(0x82, data); // FIN=1, Opcode=2 (Binary)
}
```

### 消息接收

```java
private void startMessageLoop() {
    Thread messageThread = new Thread(() -> {
        try {
            while (connected) {
                WebSocketFrame frame = readFrame();
                if (frame != null) {
                    handleFrame(frame);
                }
            }
        } catch (IOException e) {
            log.error("Message loop error", e);
            connected = false;
        }
    });
    messageThread.setDaemon(true);
    messageThread.start();
}

private WebSocketFrame readFrame() throws IOException {
    // 读取帧头部
    int firstByte = in.readByte() & 0xFF;
    int secondByte = in.readByte() & 0xFF;
    
    boolean fin = (firstByte & 0x80) != 0;
    int opcode = firstByte & 0x0F;
    boolean mask = (secondByte & 0x80) != 0;
    int payloadLength = secondByte & 0x7F;
    
    // 处理扩展载荷长度
    if (payloadLength == 126) {
        payloadLength = in.readShort() & 0xFFFF;
    } else if (payloadLength == 127) {
        payloadLength = (int) in.readLong();
    }
    
    // 读取掩码密钥
    byte[] maskingKey = null;
    if (mask) {
        maskingKey = new byte[4];
        in.readFully(maskingKey);
    }
    
    // 读取载荷数据
    byte[] payload = new byte[payloadLength];
    in.readFully(payload);
    
    // 解掩码
    if (mask && maskingKey != null) {
        for (int i = 0; i < payload.length; i++) {
            payload[i] ^= maskingKey[i % 4];
        }
    }
    
    return new WebSocketFrame(fin, opcode, mask, payload);
}
```

## 服务器端实现

### 握手处理

```java
public class WebSocketServer {
    private ServerSocket serverSocket;
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            
            // 处理HTTP握手
            if (handleHandshake(in, out)) {
                // 创建WebSocket会话
                WebSocketSession session = new WebSocketSession(clientSocket, out);
                sessions.put(session.getId(), session);
                
                // 开始消息处理
                handleWebSocketMessages(session, in);
            }
            
        } catch (IOException e) {
            log.error("Client handling error", e);
        }
    }
    
    private boolean handleHandshake(DataInputStream in, DataOutputStream out) throws IOException {
        // 读取HTTP请求
        StringBuilder request = new StringBuilder();
        String line;
        String secWebSocketKey = null;
        
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            request.append(line).append("\r\n");
            if (line.startsWith("Sec-WebSocket-Key:")) {
                secWebSocketKey = line.substring(19).trim();
            }
        }
        
        if (secWebSocketKey == null) {
            return false;
        }
        
        // 生成响应密钥
        String secWebSocketAccept = generateAcceptKey(secWebSocketKey);
        
        // 发送HTTP响应
        String response = String.format(
            "HTTP/1.1 101 Switching Protocols\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: %s\r\n" +
            "\r\n",
            secWebSocketAccept
        );
        
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
        
        return true;
    }
    
    private String generateAcceptKey(String secWebSocketKey) {
        String concatenated = secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(concatenated.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
}
```

### 消息处理

```java
private void handleWebSocketMessages(WebSocketSession session, DataInputStream in) {
    try {
        while (true) {
            WebSocketFrame frame = readFrame(in);
            if (frame == null) break;
            
            switch (frame.getOpcode()) {
                case 0x1: // Text
                    handleTextMessage(session, frame);
                    break;
                case 0x2: // Binary
                    handleBinaryMessage(session, frame);
                    break;
                case 0x8: // Close
                    handleCloseMessage(session, frame);
                    break;
                case 0x9: // Ping
                    handlePingMessage(session, frame);
                    break;
                case 0xA: // Pong
                    handlePongMessage(session, frame);
                    break;
            }
        }
    } catch (IOException e) {
        log.error("WebSocket message handling error", e);
    } finally {
        sessions.remove(session.getId());
    }
}

private void handleTextMessage(WebSocketSession session, WebSocketFrame frame) {
    String message = new String(frame.getPayload(), StandardCharsets.UTF_8);
    log.info("Received text message from {}: {}", session.getId(), message);
    
    // 处理业务逻辑
    String response = processTextMessage(message);
    
    // 发送响应
    session.sendTextMessage(response);
}

private void handleBinaryMessage(WebSocketSession session, WebSocketFrame frame) {
    byte[] data = frame.getPayload();
    log.info("Received binary message from {}: {} bytes", session.getId(), data.length);
    
    // 处理二进制数据
    byte[] response = processBinaryMessage(data);
    
    // 发送响应
    session.sendBinaryMessage(response);
}
```

## 会话管理

```java
public class WebSocketSession {
    private final String id;
    private final Socket socket;
    private final DataOutputStream out;
    private final long connectTime;
    private final AtomicLong lastPing;
    
    public WebSocketSession(Socket socket, DataOutputStream out) {
        this.id = "session-" + System.currentTimeMillis();
        this.socket = socket;
        this.out = out;
        this.connectTime = System.currentTimeMillis();
        this.lastPing = new AtomicLong(System.currentTimeMillis());
    }
    
    public void sendTextMessage(String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        byte[] frame = createTextFrame(data);
        out.write(frame);
        out.flush();
    }
    
    public void sendBinaryMessage(byte[] data) throws IOException {
        byte[] frame = createBinaryFrame(data);
        out.write(frame);
        out.flush();
    }
    
    public void sendPing(byte[] payload) throws IOException {
        byte[] frame = createPingFrame(payload);
        out.write(frame);
        out.flush();
    }
    
    public void sendPong(byte[] payload) throws IOException {
        byte[] frame = createPongFrame(payload);
        out.write(frame);
        out.flush();
    }
    
    public void close(int code, String reason) throws IOException {
        byte[] frame = createCloseFrame(code, reason);
        out.write(frame);
        out.flush();
        socket.close();
    }
}
```

## 心跳机制

### Ping/Pong实现

```java
public class WebSocketHeartbeat {
    private final WebSocketSession session;
    private final ScheduledExecutorService scheduler;
    private final long pingInterval;
    
    public WebSocketHeartbeat(WebSocketSession session, long pingInterval) {
        this.session = session;
        this.pingInterval = pingInterval;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startHeartbeat();
    }
    
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 发送Ping
                byte[] pingData = "ping".getBytes(StandardCharsets.UTF_8);
                session.sendPing(pingData);
                
                // 设置超时检查
                scheduler.schedule(() -> {
                    if (System.currentTimeMillis() - session.getLastPing() > 30000) {
                        // 超时，关闭连接
                        session.close(1000, "Ping timeout");
                    }
                }, 5000, TimeUnit.MILLISECONDS);
                
            } catch (IOException e) {
                log.error("Heartbeat ping failed", e);
            }
        }, pingInterval, pingInterval, TimeUnit.MILLISECONDS);
    }
    
    public void onPong() {
        session.updateLastPing();
    }
}
```

## 性能优化

### 连接池

```java
public class WebSocketConnectionPool {
    private final BlockingQueue<WebSocketClient> connections;
    private final String host;
    private final int port;
    private final String path;
    
    public WebSocketConnectionPool(String host, int port, String path, int poolSize) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.connections = new LinkedBlockingQueue<>(poolSize);
        
        // 预创建连接
        for (int i = 0; i < poolSize; i++) {
            try {
                WebSocketClient client = new WebSocketClient();
                client.connect(host, port, path);
                connections.offer(client);
            } catch (IOException e) {
                log.error("Failed to create connection", e);
            }
        }
    }
    
    public WebSocketClient getConnection() throws IOException {
        WebSocketClient client = connections.poll();
        if (client == null || !client.isConnected()) {
            client = new WebSocketClient();
            client.connect(host, port, path);
        }
        return client;
    }
    
    public void returnConnection(WebSocketClient client) {
        if (client != null && client.isConnected()) {
            connections.offer(client);
        }
    }
}
```

### 消息批处理

```java
public class WebSocketMessageBatcher {
    private final List<WebSocketMessage> batch = new ArrayList<>();
    private final int maxBatchSize;
    private final WebSocketSession session;
    
    public void addMessage(WebSocketMessage message) {
        synchronized (batch) {
            batch.add(message);
            if (batch.size() >= maxBatchSize) {
                flushBatch();
            }
        }
    }
    
    private void flushBatch() {
        List<WebSocketMessage> messagesToSend;
        synchronized (batch) {
            if (batch.isEmpty()) return;
            messagesToSend = new ArrayList<>(batch);
            batch.clear();
        }
        
        // 批量发送消息
        for (WebSocketMessage message : messagesToSend) {
            try {
                if (message.isText()) {
                    session.sendTextMessage(message.getContent());
                } else {
                    session.sendBinaryMessage(message.getData());
                }
            } catch (IOException e) {
                log.error("Failed to send batched message", e);
            }
        }
    }
}
```

## 监控和诊断

### 连接统计

```java
public class WebSocketStatistics {
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicLong pingCount = new AtomicLong(0);
    private final AtomicLong pongCount = new AtomicLong(0);
    
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
    
    public void recordPing() {
        pingCount.incrementAndGet();
    }
    
    public void recordPong() {
        pongCount.incrementAndGet();
    }
}
```

### 健康检查

```java
public class WebSocketHealthCheck {
    private final String host;
    private final int port;
    private final String path;
    
    public boolean isHealthy() {
        try (WebSocketClient client = new WebSocketClient()) {
            client.connect(host, port, path);
            client.sendPing("health-check".getBytes());
            return client.waitForPong(5000);
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 测试用例

### 单元测试

```java
@Test
public void testWebSocketConnection() throws Exception {
    WebSocketClient client = new WebSocketClient();
    client.connect("localhost", 8081, "/chat");
    assertTrue(client.isConnected());
    client.close();
}

@Test
public void testTextMessage() throws Exception {
    WebSocketClient client = new WebSocketClient();
    client.connect("localhost", 8081, "/chat");
    
    String message = "Hello WebSocket";
    client.sendTextMessage(message);
    
    String response = client.receiveTextMessage();
    assertNotNull(response);
    
    client.close();
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
                WebSocketClient client = new WebSocketClient();
                client.connect("localhost", 8081, "/chat");
                
                for (int j = 0; j < messagesPerClient; j++) {
                    client.sendTextMessage("Message " + j);
                }
                
                client.close();
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
# WebSocket服务器配置
websocket.port=8081
websocket.max.connections=10000
websocket.ping.interval=30000
websocket.ping.timeout=5000
websocket.max.message.size=65536
websocket.buffer.size=8192
```

### 客户端配置

```properties
# WebSocket客户端配置
websocket.server.host=localhost
websocket.server.port=8081
websocket.path=/chat
websocket.ping.interval=30000
websocket.connection.timeout=30000
websocket.retry.count=3
```

## 故障排除

### 常见问题

1. **握手失败**
   - 检查HTTP头格式
   - 验证Sec-WebSocket-Key
   - 确认服务器支持WebSocket

2. **连接断开**
   - 检查网络稳定性
   - 验证心跳机制
   - 调整超时设置

3. **性能问题**
   - 优化消息大小
   - 使用消息批处理
   - 调整缓冲区设置

### 调试工具

```bash
# 使用wscat测试WebSocket
wscat -c ws://localhost:8081/chat

# 使用curl测试HTTP握手
curl -i -N -H "Connection: Upgrade" \
     -H "Upgrade: websocket" \
     -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
     -H "Sec-WebSocket-Version: 13" \
     http://localhost:8081/chat

# 使用浏览器开发者工具
# 在浏览器中打开WebSocket连接进行测试
```

## 版本历史

- **v1.0.0** - 基本WebSocket支持
- **v1.1.0** - 添加心跳机制
- **v1.2.0** - 支持二进制消息
- **v1.3.0** - 性能优化和监控改进
