package com.dtc.core.persistence.transaction.distributed;

import java.io.Serializable;

/**
 * 全局事务实体
 */
public class GlobalTransaction implements Serializable {
    private String xid;
    private TransactionStatus status;
    private long startTime;
    private long endTime;
    
    public GlobalTransaction() {}
    
    public GlobalTransaction(String xid) {
        this.xid = xid;
        this.status = TransactionStatus.BEGIN;
        this.startTime = System.currentTimeMillis();
    }
    
    public String getXid() { return xid; }
    public void setXid(String xid) { this.xid = xid; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
}

