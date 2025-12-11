package com.dtc.framework.persistent.transaction.interceptor;

import com.dtc.framework.aop.support.AnnotationMatchingPointcut;
import com.dtc.framework.aop.support.DefaultPointcutAdvisor;
import com.dtc.framework.persistent.transaction.PlatformTransactionManager;
import com.dtc.framework.persistent.transaction.annotation.Transactional;

public class TransactionAttributeSourceAdvisor extends DefaultPointcutAdvisor {
    public TransactionAttributeSourceAdvisor(PlatformTransactionManager transactionManager) {
        super(new AnnotationMatchingPointcut(Transactional.class), createInterceptor(transactionManager));
    }
    
    private static TransactionInterceptor createInterceptor(PlatformTransactionManager transactionManager) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        return interceptor;
    }
}

