package com.dtc.core.persistence.transaction.distributed.impl;

import com.dtc.core.persistence.DataSourceProvider;
import com.dtc.core.persistence.transaction.distributed.GlobalTransaction;
import com.dtc.core.persistence.transaction.distributed.GlobalTransactionManager;
import com.dtc.core.persistence.transaction.distributed.TransactionStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

/**
 * 基于数据库的全局事务管理器
 */
@Singleton
public class DbBasedGlobalTransactionManager implements GlobalTransactionManager {

    private static final Logger log = LoggerFactory.getLogger(DbBasedGlobalTransactionManager.class);
    
    @Inject
    private DataSourceProvider dataSourceProvider;

    private static final String TABLE_NAME = "global_transactions";

    @Inject
    public void init() {
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "xid VARCHAR(64) PRIMARY KEY, " +
                    "status VARCHAR(20), " +
                    "start_time BIGINT, " +
                    "end_time BIGINT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            log.warn("Failed to create global_transactions table: {}", e.getMessage());
        }
    }

    @Override
    public String begin() {
        String xid = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        String sql = "INSERT INTO " + TABLE_NAME + " (xid, status, start_time) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, xid);
            stmt.setString(2, TransactionStatus.BEGIN.name());
            stmt.setLong(3, startTime);
            stmt.executeUpdate();
            return xid;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin global transaction", e);
        }
    }

    @Override
    public void commit(String xid) {
        updateStatus(xid, TransactionStatus.COMMITTED);
    }

    @Override
    public void rollback(String xid) {
        updateStatus(xid, TransactionStatus.ROLLED_BACK);
    }

    @Override
    public GlobalTransaction getTransaction(String xid) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE xid = ?";
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, xid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GlobalTransaction tx = new GlobalTransaction();
                    tx.setXid(rs.getString("xid"));
                    tx.setStatus(TransactionStatus.valueOf(rs.getString("status")));
                    tx.setStartTime(rs.getLong("start_time"));
                    tx.setEndTime(rs.getLong("end_time"));
                    return tx;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get transaction", e);
        }
        return null;
    }

    private void updateStatus(String xid, TransactionStatus status) {
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, end_time = ? WHERE xid = ?";
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, xid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction status to " + status, e);
        }
    }
}

