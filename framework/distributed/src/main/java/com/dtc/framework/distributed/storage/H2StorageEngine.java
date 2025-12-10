package com.dtc.framework.distributed.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 H2 的轻量级存储引擎示例
 * 展示了如何启动嵌入式 H2 并执行基础 SQL 操作
 */
@Singleton
public class H2StorageEngine {

    private static final Logger log = LoggerFactory.getLogger(H2StorageEngine.class);
    private final HikariDataSource dataSource;

    public H2StorageEngine() {
        // 配置嵌入式 H2
        // mode=MySQL 为了兼容性，DB_CLOSE_DELAY=-1 保持 JVM 运行期间数据库常驻内存
        String jdbcUrl = "jdbc:h2:mem:distributed_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false";
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        
        this.dataSource = new HikariDataSource(config);
        
        initSchema();
    }

    private void initSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建一个示例 Key-Value 表
            stmt.execute("CREATE TABLE IF NOT EXISTS kv_store (" +
                         "k VARCHAR(255) PRIMARY KEY, " +
                         "v VARCHAR(255), " +
                         "ts BIGINT)");
                         
            log.info("H2 Storage initialized successfully.");
        } catch (SQLException e) {
            log.error("Failed to init H2 schema", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行更新 (用于 Raft Apply)
     */
    public CompletableFuture<Integer> update(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL execution failed: " + sql, e);
            }
        });
    }

    /**
     * 执行查询 (用于本地读)
     */
    public List<String> query(String sql, Object... params) {
        List<String> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                int colCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= colCount; i++) {
                        row.append(rs.getObject(i)).append(",");
                    }
                    results.add(row.toString());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
        return results;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * 备份数据库到指定文件 (用于 Raft Snapshot Save)
     */
    public void backup(String filePath) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // SCRIPT TO 'fileName'
            // 使用 COMPRESSION ZIP 压缩以节省空间
            String sql = String.format("SCRIPT TO '%s' COMPRESSION ZIP", filePath);
            stmt.execute(sql);
            log.info("Database backup to {} success", filePath);
        }
    }

    /**
     * 从指定文件恢复数据库 (用于 Raft Snapshot Load)
     */
    public void restore(String filePath) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 先清除现有数据（可选，取决于 H2 行为，但在 RUNSCRIPT 前通常最好是空的或者 DROP ALL OBJECTS）
            stmt.execute("DROP ALL OBJECTS");
            
            // RUNSCRIPT FROM 'fileName'
            String sql = String.format("RUNSCRIPT FROM '%s' COMPRESSION ZIP", filePath);
            stmt.execute(sql);
            log.info("Database restore from {} success", filePath);
        }
    }
}

