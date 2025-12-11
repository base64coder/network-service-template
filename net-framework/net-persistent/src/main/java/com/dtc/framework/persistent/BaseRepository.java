package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 基础Repository接口
 * 提供通用的CRUD操作方法
 * 参考MyBatis-Flex的BaseMapper和Spring Data JPA的Repository
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * 
 * @author Network Service Template
 */
public interface BaseRepository<T, ID> {
    
    /**
     * 根据ID查找实体
     * 
     * @param id 主键ID
     * @return 实体对象，如果不存在返回null
     */
    @Nullable
    T findById(@NotNull ID id);
    
    /**
     * 根据ID查找实体并返回Optional
     * 
     * @param id 主键ID
     * @return Optional包装的实体对象
     */
    @NotNull
    Optional<T> findByIdOptional(@NotNull ID id);
    
    /**
     * 查找所有实体
     * 
     * @return 实体列表
     */
    @NotNull
    List<T> findAll();
    
    /**
     * 保存实体，如果已存在则更新
     * 
     * @param entity 实体对象
     * @return 保存后的实体对象
     */
    @NotNull
    T save(@NotNull T entity);
    
    /**
     * 批量保存实体
     * 
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    @NotNull
    List<T> saveAll(@NotNull List<T> entities);
    
    /**
     * 根据ID删除实体
     * 
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean deleteById(@NotNull ID id);
    
    /**
     * 删除实体
     * 
     * @param entity 实体对象
     * @return 是否删除成功
     */
    boolean delete(@NotNull T entity);
    
    /**
     * 批量删除实体
     * 
     * @param entities 实体列表
     * @return 删除的数量
     */
    int deleteAll(@NotNull List<T> entities);
    
    /**
     * 根据ID检查实体是否存在
     * 
     * @param id 主键ID
     * @return 是否存在
     */
    boolean existsById(@NotNull ID id);
    
    /**
     * 统计实体数量
     * 
     * @return 数量
     */
    long count();
}
