# HTTP REST 协议扩展

## 📋 概述

HTTP REST 协议扩展为网络服务模板提供了完整的 HTTP REST API 支持，包括请求处理、路由管理、中间件支持、认证授权、限流等功能。

## 🚀 主要功能

### 1. HTTP 服务器
- 基于 Netty 的高性能 HTTP 服务器
- 支持 HTTP/1.1 协议
- 异步非阻塞处理
- 连接池管理
- 超时控制

### 2. 路由管理
- RESTful 路由支持
- 路径参数提取
- 方法匹配
- 路由优先级
- 动态路由注册

### 3. 中间件支持
- CORS 跨域处理
- 请求日志记录
- 认证授权
- 请求限流
- 可扩展中间件架构

### 4. 请求处理
- JSON 数据解析
- 表单数据处理
- 查询参数解析
- 路径参数提取
- 请求验证

### 5. 响应处理
- JSON 响应生成
- 错误处理
- 状态码管理
- 响应头设置
- 内容类型处理

## 🔧 配置参数

### 基础配置
```xml
<port>8080</port>                    <!-- HTTP 服务端口 -->
<max-connections>1000</max-connections>  <!-- 最大连接数 -->
<request-timeout>30000</request-timeout> <!-- 请求超时时间(ms) -->
```

### CORS 配置
```xml
<cors-enabled>true</cors-enabled>    <!-- 启用 CORS -->
<cors-origins>*</cors-origins>       <!-- 允许的源 -->
<cors-methods>GET, POST, PUT, DELETE, OPTIONS</cors-methods>  <!-- 允许的方法 -->
<cors-headers>Content-Type, Authorization</cors-headers>     <!-- 允许的头部 -->
```

### 限流配置
```xml
<rate-limit-enabled>true</rate-limit-enabled>        <!-- 启用限流 -->
<rate-limit-max-requests>100</rate-limit-max-requests>  <!-- 最大请求数 -->
<rate-limit-window-ms>60000</rate-limit-window-ms>   <!-- 时间窗口(ms) -->
```

### 认证配置
```xml
<auth-enabled>true</auth-enabled>    <!-- 启用认证 -->
<public-paths>/,/health,/status,/api/info</public-paths>  <!-- 公开路径 -->
<admin-paths>/api/admin</admin-paths>  <!-- 管理员路径 -->
```

## 📝 API 端点

### 基础端点
- `GET /` - 欢迎页面
- `GET /health` - 健康检查
- `GET /status` - 服务状态
- `GET /api/info` - API 信息

### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 订单管理
- `GET /api/orders` - 获取订单列表
- `POST /api/orders` - 创建订单
- `GET /api/orders/{id}` - 获取订单详情

### 产品管理
- `GET /api/products` - 获取产品列表
- `POST /api/products` - 创建产品
- `GET /api/products/{id}` - 获取产品详情

## 💻 使用示例

### 1. 创建 HTTP 扩展实例

```java
@Inject
private HttpExtension httpExtension;

// 启动扩展
httpExtension.extensionStart();
```

### 2. 注册自定义路由

```java
@Inject
private HttpRouteManager routeManager;

// 注册 GET 路由
routeManager.registerGet("/api/custom", request -> {
    return new HttpResponse.Builder()
        .ok()
        .jsonContent()
        .body("{\"message\":\"Hello from custom endpoint\"}")
        .build();
});

// 注册 POST 路由
routeManager.registerPost("/api/custom", request -> {
    String body = request.getBody();
    // 处理请求体
    return new HttpResponse.Builder()
        .created()
        .jsonContent()
        .body("{\"message\":\"Created successfully\"}")
        .build();
});
```

### 3. 添加自定义中间件

```java
@Inject
private HttpMiddlewareManager middlewareManager;

// 添加自定义中间件
middlewareManager.addMiddleware(new CustomMiddleware());
```

### 4. 处理请求和响应

```java
@Inject
private HttpRequestHandler requestHandler;
private HttpResponseHandler responseHandler;

// 处理请求
HttpResponse response = requestHandler.handleRequest(request);

// 发送响应
responseHandler.sendResponse(clientId, response);
```

## 🔒 认证和授权

### 1. 基本认证
```bash
# 发送认证请求
curl -H "Authorization: Bearer your-token" \
     http://localhost:8080/api/users
```

### 2. 管理员访问
```bash
# 管理员请求
curl -H "Authorization: Bearer admin_token" \
     http://localhost:8080/api/admin/users
```

## 🚦 限流控制

### 1. 配置限流
```xml
<rate-limit-max-requests>100</rate-limit-max-requests>
<rate-limit-window-ms>60000</rate-limit-window-ms>
```

### 2. 限流响应
```json
{
  "error": "Rate Limit Exceeded",
  "message": "Too many requests"
}
```

## 📊 监控和统计

### 1. 服务器统计
```java
HttpServer.HttpServerStats stats = httpServer.getStats();
System.out.println("Active connections: " + stats.getActiveConnections());
System.out.println("Total clients: " + stats.getTotalClients());
```

### 2. 请求统计
```java
HttpRequestHandler.HttpRequestStats stats = requestHandler.getStats();
System.out.println("Processed requests: " + stats.getProcessedRequests());
System.out.println("Error rate: " + stats.getErrorRate());
```

### 3. 响应统计
```java
HttpResponseHandler.HttpResponseStats stats = responseHandler.getStats();
System.out.println("Sent responses: " + stats.getSentResponses());
System.out.println("Error responses: " + stats.getErrorResponses());
```

## 🛠️ 自定义开发

### 1. 创建自定义中间件

```java
public class CustomMiddleware implements HttpMiddleware {
    @Override
    public HttpResponse beforeRequest(HttpRequest request) {
        // 请求前处理逻辑
        return null;
    }
    
    @Override
    public HttpResponse afterRequest(HttpRequest request, HttpResponse response) {
        // 请求后处理逻辑
        return response;
    }
}
```

### 2. 创建自定义路由处理器

```java
public class CustomRouteHandler implements HttpRouteHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        // 处理逻辑
        return new HttpResponse.Builder()
            .ok()
            .jsonContent()
            .body("{\"message\":\"Custom response\"}")
            .build();
    }
}
```

## 🔧 故障排除

### 1. 常见问题

#### 连接超时
- 检查 `request-timeout` 配置
- 检查网络连接
- 检查服务器负载

#### 认证失败
- 检查 Authorization 头部
- 检查令牌有效性
- 检查公开路径配置

#### 限流触发
- 检查请求频率
- 调整限流参数
- 检查客户端实现

### 2. 日志配置

```xml
<logger name="com.dtc.core.http" level="DEBUG"/>
<logger name="com.dtc.http" level="INFO"/>
```

## 📚 参考资料

- [HTTP/1.1 规范](https://tools.ietf.org/html/rfc2616)
- [RESTful API 设计指南](https://restfulapi.net/)
- [Netty 官方文档](https://netty.io/)
- [Jackson JSON 处理](https://github.com/FasterXML/jackson)

## 🔄 版本历史

- **v1.0.0** - 初始版本，基础 HTTP 服务器功能
- **v1.1.0** - 添加路由管理和中间件支持
- **v1.2.0** - 添加认证授权和限流功能
- **v1.3.0** - 性能优化和监控功能
