package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表注解
 * 标识实体类对应的数据库表
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    
    /**
     * 表名称
     * @return 表名
     */
    String value();
    
    /**
     * 数据库schema（模式）
     * @return schema名称
     */
    String schema() default "";
    
    /**
     * 是否将驼峰属性转换为下划线字段
     * 默认为true
     * @return 是否转换
     */
    boolean camelToUnderline() default true;
    
    /**
     * 表注释
     * @return 注释内容
     */
    String comment() default "";
}
