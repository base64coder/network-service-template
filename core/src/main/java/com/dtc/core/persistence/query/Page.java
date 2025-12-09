package com.dtc.core.persistence.query;

import com.dtc.api.annotations.NotNull;
import java.util.List;

/**
 * 分页对象
 * 参考 MyBatis-Flex 的 Page 设计
 * 
 * @param <T> 实体类型
 * 
 * @author Network Service Template
 */
public class Page<T> {
    
    private final List<T> records;
    private final long total;
    private final int pageNumber;
    private final int pageSize;
    
    public Page(@NotNull List<T> records, long total, int pageNumber, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
    
    /**
     * 获取记录列表
     */
    @NotNull
    public List<T> getRecords() {
        return records;
    }
    
    /**
     * 获取总记录数
     */
    public long getTotal() {
        return total;
    }
    
    /**
     * 获取当前页码
     */
    public int getPageNumber() {
        return pageNumber;
    }
    
    /**
     * 获取每页大小
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * 获取总页数
     */
    public long getTotalPages() {
        return pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
    }
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNumber > 1;
    }
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }
}

