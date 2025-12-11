package com.dtc.core.persistence.transaction.distributed;

import com.dtc.annotations.transaction.DistributedTransactional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;

public class DistributedTransactionInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DistributedTransactionInterceptor.class);

    @Inject
    private GlobalTransactionManager transactionManager;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        DistributedTransactional annotation = invocation.getMethod().getAnnotation(DistributedTransactional.class);
        if (annotation == null) {
            annotation = invocation.getMethod().getDeclaringClass().getAnnotation(DistributedTransactional.class);
        }

        if (annotation == null) {
            return invocation.proceed();
        }

        // 如果已经有全局事务，加入它（简单实现）
        if (TransactionContext.getXID() != null) {
            return invocation.proceed();
        }

        String xid = transactionManager.begin();
        TransactionContext.setXID(xid);
        log.info("Began distributed transaction: {}", xid);

        try {
            Object result = invocation.proceed();
            transactionManager.commit(xid);
            log.info("Committed distributed transaction: {}", xid);
            return result;
        } catch (Throwable t) {
            transactionManager.rollback(xid);
            log.error("Rolled back distributed transaction: {}", xid, t);
            throw t;
        } finally {
            TransactionContext.clear();
        }
    }
}

