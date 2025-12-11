package com.dtc.core.persistence.transaction.distributed;

/**
 * 分布式事务状态
 */
public enum TransactionStatus {
    BEGIN,          // 事务开始
    COMMITTING,     // 正在提交
    COMMITTED,      // 已提交
    ROLLING_BACK,   // 正在回滚
    ROLLED_BACK,    // 已回滚
    FAILED          // 失败
}

