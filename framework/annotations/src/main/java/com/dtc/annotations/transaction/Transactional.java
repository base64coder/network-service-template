package com.dtc.annotations.transaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务注解
 * 用于标记需要事务管理的方法或类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    /**
     * 是否只读事务
     * 只读事务可能会被路由到从库
     */
    boolean readOnly() default false;

    /**
     * 事务传播行为（预留）
     */
    // Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务隔离级别（预留）
     */
    // Isolation isolation() default Isolation.DEFAULT;
}

