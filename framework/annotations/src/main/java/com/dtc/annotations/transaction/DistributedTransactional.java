package com.dtc.annotations.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式事务注解
 * 标记该方法需要参与分布式事务
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedTransactional {
    /**
     * 事务超时时间（毫秒）
     */
    long timeout() default 60000;
}

