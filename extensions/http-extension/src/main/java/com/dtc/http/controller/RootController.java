package com.dtc.http.controller;

import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.annotations.web.GetMapping;
import com.dtc.annotations.web.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * 根路径控制器
 * 提供根路径和系统信息相关的 REST API 接口
 * 
 * @author Network Service Template
 */
@RestController
@Singleton
public class RootController {

    private static final Logger log = LoggerFactory.getLogger(RootController.class);

    private final StatisticsCollector statisticsCollector;

    @Inject
    public RootController(StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    /**
     * 根路径
     * 返回欢迎信息和系统基本信息，以 JSON 格式返回
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "message", "Welcome to HTTP REST API",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis());
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis());
    }

    /**
     * 系统状态
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "status", "running",
                "uptime", System.currentTimeMillis(),
                "activeConnections", statisticsCollector.getActiveConnections(),
                "totalRequests", statisticsCollector.getTotalRequests());
    }

    /**
     * API信息
     */
    @GetMapping("/api/info")
    public Map<String, Object> apiInfo() {
        return Map.of(
                "name", "Network Service Template HTTP API",
                "version", "1.0.0",
                "description", "RESTful API for network service template",
                "endpoints", Map.of(
                        "users", "/api/users",
                        "orders", "/api/orders",
                        "products", "/api/products"));
    }
}
