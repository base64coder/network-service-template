package com.dtc.framework.persistent;

import java.util.List;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.persistent.query.Page;
import com.dtc.framework.persistent.query.QueryWrapper;

/**
 * 扩展的 Repository 接口
 * 提供更丰富的查询功能
 * 参考 MyBatis-Flex 的 BaseMapper 和 Hibernate 的 Repository
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * 
 * @author Network Service Template
 */
public interface ExtendedRepository<T, ID> extends BaseRepository<T, ID> {
    
    /**
     * 根据条件查询单个实体
     * 
     * @param wrapper 查询包装器
     * @return 实体对象
     */
    @Nullable
    T selectOne(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * 根据条件查询列表
     * 
     * @param wrapper 查询包装器
     * @return 实体列表
     */
    @NotNull
    List<T> selectList(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * 根据条件查询数量
     * 
     * @param wrapper 查询包装器
     * @return 数量
     */
    long selectCount(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * 分页查询
     * 
     * @param pageNumber 页码（从1开始）
     * @param pageSize 每页大小
     * @param wrapper 查询包装器
     * @return 分页对象
     */
    @NotNull
    Page<T> selectPage(int pageNumber, int pageSize, @NotNull QueryWrapper<T> wrapper);
    
    /**
     * 根据条件更新
     * 
     * @param entity 实体对象
     * @param wrapper 查询包装器
     * @return 更新的行数
     */
    int updateByWrapper(@NotNull T entity, @NotNull QueryWrapper<T> wrapper);
    
    /**
     * 根据条件删除
     * 
     * @param wrapper 查询包装器
     * @return 删除的行数
     */
    int deleteByWrapper(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * 批量插入
     * 
     * @param entities 实体列表
     * @return 插入的行数
     */
    int insertBatch(@NotNull List<T> entities);
    
    /**
     * 批量更新
     * 
     * @param entities 实体列表
     * @return 更新的行数
     */
    int updateBatch(@NotNull List<T> entities);
}

