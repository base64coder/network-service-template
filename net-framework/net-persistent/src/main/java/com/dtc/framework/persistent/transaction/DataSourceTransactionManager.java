package com.dtc.framework.persistent.transaction;

import com.dtc.framework.persistent.transaction.support.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager implements PlatformTransactionManager {
    private DataSource dataSource;

    public DataSourceTransactionManager() {}
    
    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        
        if (conHolder != null) {
            return new DefaultTransactionStatus(conHolder, false);
        }
        
        try {
            Connection con = dataSource.getConnection();
            con.setAutoCommit(false);
            if (definition != null && definition.isReadOnly()) {
                try {
                    con.setReadOnly(true);
                } catch (Throwable ex) {
                    // ignore
                }
            }
            
            ConnectionHolder newHolder = new ConnectionHolder(con);
            TransactionSynchronizationManager.bindResource(dataSource, newHolder);
            
            return new DefaultTransactionStatus(newHolder, true);
        } catch (SQLException e) {
            throw new TransactionException("Cannot open connection", e);
        }
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        if (defStatus.isCompleted()) {
            throw new TransactionException("Transaction is already completed");
        }
        
        if (defStatus.isRollbackOnly()) {
            rollback(status);
            return;
        }
        
        if (defStatus.isNewTransaction()) {
            ConnectionHolder holder = (ConnectionHolder) defStatus.getTransaction();
            try {
                holder.getConnection().commit();
            } catch (SQLException e) {
                throw new TransactionException("Commit failed", e);
            } finally {
                cleanup(holder);
            }
        }
        
        defStatus.setCompleted();
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        if (defStatus.isCompleted()) {
            throw new TransactionException("Transaction is already completed");
        }
        
        if (defStatus.isNewTransaction()) {
            ConnectionHolder holder = (ConnectionHolder) defStatus.getTransaction();
            try {
                holder.getConnection().rollback();
            } catch (SQLException e) {
                throw new TransactionException("Rollback failed", e);
            } finally {
                cleanup(holder);
            }
        } else {
            // Mark existing transaction as rollback-only if possible
             if (defStatus.getTransaction() instanceof ConnectionHolder) {
                 // But we can't easily mark ConnectionHolder. 
                 // Real Spring uses TransactionObject.
                 // We will skip for simplified P2.
             }
        }
        
        defStatus.setCompleted();
    }
    
    private void cleanup(ConnectionHolder holder) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        try {
            holder.getConnection().setAutoCommit(true);
            holder.getConnection().close();
        } catch (SQLException e) {
            // ignore
        }
    }
}

