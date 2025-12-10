package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多对多关联关系注解
 * 标识实体类之间的多对多关系
 * 
 * @author Network Service Template
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ManyToMany {
    
    /**
     * 当前实体类的关联字段
     * 如果为空，则使用当前实体类的主键
     * @return 字段名
     */
    String selfField() default "";
    
    /**
     * 目标实体类的关联字段
     * 如果为空，则使用目标实体类的主键
     * @return 字段名
     */
    String targetField() default "";
    
    /**
     * 目标实体类对应的表名
     * 如果目标实体类使用了@Table注解，可以省略
     * @return 表名
     */
    String targetTable() default "";
    
    /**
     * 中间表名称（必需）
     * @return 中间表名
     */
    String joinTable();
    
    /**
     * 中间表与当前表的关联字段
     * @return 字段名
     */
    String joinSelfColumn();
    
    /**
     * 中间表与目标表的关联字段
     * @return 字段名
     */
    String joinTargetColumn();
    
    /**
     * 是否立即加载（EAGER）还是延迟加载（LAZY）
     * @return 加载策略
     */
    FetchType fetch() default FetchType.LAZY;
    
    /**
     * 级联操作类型
     * @return 级联类型数组
     */
    CascadeType[] cascade() default {};
    
    /**
     * 查询排序
     * @return 排序SQL
     */
    String orderBy() default "";
    
    /**
     * 查询时的额外条件
     * @return SQL条件
     */
    String extraCondition() default "";
    
    /**
     * 当映射为Map时，使用哪个字段作为Key
     * @return 字段名
     */
    String mapKeyField() default "";
}
