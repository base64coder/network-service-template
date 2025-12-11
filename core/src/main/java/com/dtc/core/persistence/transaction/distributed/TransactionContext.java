package com.dtc.core.persistence.transaction.distributed;

/**
 * 分布式事务上下文
 * 用于在线程间传递 XID
 */
public class TransactionContext {
    private static final ThreadLocal<String> CURRENT_XID = new ThreadLocal<>();

    public static String getXID() {
        return CURRENT_XID.get();
    }

    public static void setXID(String xid) {
        CURRENT_XID.set(xid);
    }

    public static void clear() {
        CURRENT_XID.remove();
    }
}

