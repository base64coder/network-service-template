package com.dtc.framework.persistent.transaction.support;

import com.dtc.framework.persistent.transaction.TransactionStatus;

public class DefaultTransactionStatus implements TransactionStatus {
    private final Object transaction;
    private final boolean newTransaction;
    private boolean rollbackOnly = false;
    private boolean completed = false;

    public DefaultTransactionStatus(Object transaction, boolean newTransaction) {
        this.transaction = transaction;
        this.newTransaction = newTransaction;
    }

    public Object getTransaction() {
        return transaction;
    }

    @Override
    public boolean isNewTransaction() {
        return newTransaction;
    }

    @Override
    public boolean hasSavepoint() {
        return false;
    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted() {
        this.completed = true;
    }
}

