package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库列注解
 * 标识实体属性对应的数据库列
 * 
 * @author Network Service Template
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Column {
    
    /**
     * 列名称
     * 如果为空，则使用属性名（根据camelToUnderline转换）
     * @return 列名
     */
    String value() default "";
    
    /**
     * 是否忽略该字段
     * 如果为true，该字段不会映射到数据库
     * @return 是否忽略
     */
    boolean ignore() default false;
    
    /**
     * 是否为主键
     * @return 是否为主键
     */
    boolean primaryKey() default false;
    
    /**
     * 是否为逻辑删除字段
     * 一张表中只能有一个逻辑删除字段
     * @return 是否为逻辑删除字段
     */
    boolean logicDelete() default false;
    
    /**
     * 是否为乐观锁字段
     * 更新时会检查版本号，更新成功后版本号+1
     * 只能用于数值类型字段
     * @return 是否为乐观锁字段
     */
    boolean version() default false;
    
    /**
     * 列注释
     * @return 注释内容
     */
    String comment() default "";
    
    /**
     * 是否允许为空
     * @return 是否允许为空
     */
    boolean nullable() default true;
    
    /**
     * 字段长度（用于字符串类型）
     * @return 长度
     */
    int length() default 255;
}
