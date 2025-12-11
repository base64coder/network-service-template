package com.dtc.core.persistence.transaction.distributed;

import com.dtc.api.annotations.Nullable;

/**
 * 全局事务管理器接口
 * 负责全局事务的生命周期管理
 */
public interface GlobalTransactionManager {
    /**
     * 开启全局事务
     * @return 事务ID (XID)
     */
    String begin();

    /**
     * 提交全局事务
     * @param xid 事务ID
     */
    void commit(String xid);

    /**
     * 回滚全局事务
     * @param xid 事务ID
     */
    void rollback(String xid);

    /**
     * 获取全局事务信息
     * @param xid 事务ID
     * @return 全局事务对象，不存在则返回 null
     */
    @Nullable
    GlobalTransaction getTransaction(String xid);
}

