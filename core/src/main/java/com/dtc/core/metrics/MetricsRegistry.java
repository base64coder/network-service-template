package com.dtc.core.metrics;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标注册表
 * 管理各种性能指标
 * 
 * @author Network Service Template
 */
@Singleton
public class MetricsRegistry {

    private static final Logger log = LoggerFactory.getLogger(MetricsRegistry.class);

    private final @NotNull Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final @NotNull Map<String, Long> gauges = new ConcurrentHashMap<>();

    /**
     * 获取计数器
     * 
     * @param name 计数器名称
     * @return 计数器值
     */
    @NotNull
    public AtomicLong getCounter(@NotNull String name) {
        return counters.computeIfAbsent(name, k -> new AtomicLong(0));
    }

    /**
     * 增加计数器
     * 
     * @param name  计数器名称
     * @param delta 增加值
     */
    public void incrementCounter(@NotNull String name, long delta) {
        getCounter(name).addAndGet(delta);
    }

    /**
     * 设置仪表值
     * 
     * @param name  仪表名称
     * @param value 值
     */
    public void setGauge(@NotNull String name, long value) {
        gauges.put(name, value);
    }

    /**
     * 获取仪表值
     * 
     * @param name 仪表名称
     * @return 值
     */
    public long getGauge(@NotNull String name) {
        return gauges.getOrDefault(name, 0L);
    }

    /**
     * 获取所有计数器
     * 
     * @return 计数器映射
     */
    @NotNull
    public Map<String, Long> getAllCounters() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        counters.forEach((name, counter) -> result.put(name, counter.get()));
        return result;
    }

    /**
     * 获取所有仪表
     * 
     * @return 仪表映射
     */
    @NotNull
    public Map<String, Long> getAllGauges() {
        return Map.copyOf(gauges);
    }

    /**
     * 重置所有指标
     */
    public void reset() {
        counters.clear();
        gauges.clear();
        log.info("Metrics registry reset");
    }
}
