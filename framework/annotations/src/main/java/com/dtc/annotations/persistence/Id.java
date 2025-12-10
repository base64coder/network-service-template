package com.dtc.annotations.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键注解
 * 标识实体类的主键字段
 * 
 * @author Network Service Template
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Id {
    
    /**
     * 主键生成策略
     * @return 生成策略
     */
    KeyType keyType() default KeyType.AUTO;
    
    /**
     * 主键生成器名称（当keyType为GENERATOR时使用）
     * @return 生成器名称
     */
    String generator() default "";
    
    /**
     * 主键生成策略枚举
     */
    enum KeyType {
        /**
         * 数据库自增（AUTO_INCREMENT）
         */
        AUTO,
        
        /**
         * 手动赋值
         */
        NONE,
        
        /**
         * UUID生成
         */
        UUID,
        
        /**
         * 雪花算法ID生成
         */
        SNOWFLAKE,
        
        /**
         * 自定义生成器
         */
        GENERATOR
    }
}
