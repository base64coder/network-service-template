package com.dtc.framework.persistent.transaction.interceptor;

import com.dtc.framework.aop.MethodInterceptor;
import com.dtc.framework.aop.MethodInvocation;
import com.dtc.framework.persistent.transaction.*;
import com.dtc.framework.persistent.transaction.annotation.Transactional;
import com.dtc.framework.persistent.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

public class TransactionInterceptor implements MethodInterceptor {
    private PlatformTransactionManager transactionManager;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (transactionManager == null) {
            return invocation.proceed();
        }
        
        Method method = invocation.getMethod();
        Transactional txAnn = method.getAnnotation(Transactional.class);
        
        // Handle Proxy: if annotation is not on proxy method, check target method in superclass/target class
        if (txAnn == null) {
            Class<?> declaringClass = method.getDeclaringClass();
            Class<?> superClass = declaringClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                try {
                    Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                    txAnn = superMethod.getAnnotation(Transactional.class);
                } catch (NoSuchMethodException e) {
                    // Ignore
                }
            }
        }
        
        if (txAnn == null) {
            txAnn = method.getDeclaringClass().getAnnotation(Transactional.class);
        }
        
        if (txAnn == null) {
            return invocation.proceed();
        }
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setReadOnly(txAnn.readOnly());
        def.setName(method.getName());
        
        TransactionStatus status = transactionManager.getTransaction(def);
        Object ret;
        try {
            ret = invocation.proceed();
        } catch (Throwable ex) {
            rollbackOn(status, ex);
            throw ex;
        }
        
        transactionManager.commit(status);
        return ret;
    }
    
    private void rollbackOn(TransactionStatus status, Throwable ex) {
        if (ex instanceof RuntimeException || ex instanceof Error) {
            transactionManager.rollback(status);
        } else {
            transactionManager.commit(status);
        }
    }
}
