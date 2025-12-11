package com.dtc.framework.persistent.transaction;

import com.dtc.annotations.transaction.Transactional;
import com.dtc.framework.persistent.datasource.DataSourceContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务拦截器
 * 处理 @Transactional 注解，管理事务和数据源路由
 */
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

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

        try {
            // 设置数据源路由
            if (readOnly) {
                DataSourceContext.set(DataSourceContext.Type.SLAVE);
                log.debug("Switched to SLAVE dataSource for read-only transaction: {}", invocation.getMethod().getName());
            } else {
                DataSourceContext.set(DataSourceContext.Type.MASTER);
                log.debug("Switched to MASTER dataSource for read-write transaction: {}", invocation.getMethod().getName());
            }

            // TODO: 开启数据库事务
            // Connection conn = dataSource.getConnection();
            // conn.setAutoCommit(false);

            Object result = invocation.proceed();

            // TODO: 提交事务
            // conn.commit();

            return result;
        } catch (Throwable t) {
            // TODO: 回滚事务
            // conn.rollback();
            log.error("Transaction failed, rolling back", t);
            throw t;
        } finally {
            // 恢复之前的数据源类型
            DataSourceContext.set(previousType);
            // TODO: 关闭连接/清理资源
        }
    }
}

