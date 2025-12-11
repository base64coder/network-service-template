package com.dtc.core.persistence.transaction;

import com.dtc.annotations.transaction.Transactional;
import com.dtc.core.persistence.DataSourceProvider;
import com.dtc.core.persistence.datasource.DataSourceContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务拦截器
 * 处理 @Transactional 注解，管理事务和数据源路由
 */
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

    @Inject
    private DataSourceProvider dataSourceProvider;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Transactional transactional = invocation.getMethod().getAnnotation(Transactional.class);
        if (transactional == null) {
            transactional = invocation.getMethod().getDeclaringClass().getAnnotation(Transactional.class);
        }

        if (transactional == null) {
            return invocation.proceed();
        }

        boolean readOnly = transactional.readOnly();
        DataSourceContext.Type previousType = DataSourceContext.get();

        DataSource ds = dataSourceProvider.getDataSource();
        // Check if we already have a connection (nested transaction)
        if (ConnectionHolder.getConnection(ds) != null) {
             return invocation.proceed();
        }

        Connection conn = null;
        try {
            // 设置数据源路由
            if (readOnly) {
                DataSourceContext.set(DataSourceContext.Type.SLAVE);
                log.debug("Switched to SLAVE dataSource for read-only transaction: {}", invocation.getMethod().getName());
            } else {
                DataSourceContext.set(DataSourceContext.Type.MASTER);
                log.debug("Switched to MASTER dataSource for read-write transaction: {}", invocation.getMethod().getName());
            }

            conn = ds.getConnection();
            conn.setAutoCommit(false);
            ConnectionHolder.setConnection(ds, conn);
            log.debug("Started transaction for {}", invocation.getMethod().getName());

            Object result = invocation.proceed();

            conn.commit();
            log.debug("Committed transaction for {}", invocation.getMethod().getName());
            return result;
        } catch (Throwable t) {
            if (conn != null) {
                try {
                    conn.rollback();
                    log.debug("Rolled back transaction for {}", invocation.getMethod().getName());
                } catch (Throwable rollbackEx) {
                    log.error("Failed to rollback transaction", rollbackEx);
                }
            }
            log.error("Transaction failed, rolling back", t);
            throw t;
        } finally {
            if (conn != null) {
                ConnectionHolder.removeConnection(ds);
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Throwable closeEx) {
                    log.error("Failed to close connection", closeEx);
                }
            }
            // 恢复之前的数据源类型
            DataSourceContext.set(previousType);
        }
    }
}
