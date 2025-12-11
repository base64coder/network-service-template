package com.dtc.framework.persistent.transaction;

public interface TransactionStatus {
    boolean isNewTransaction();
    boolean hasSavepoint();
    void setRollbackOnly();
    boolean isRollbackOnly();
    void flush();
    boolean isCompleted();
}

