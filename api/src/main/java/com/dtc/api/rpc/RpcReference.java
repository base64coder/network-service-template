package com.dtc.api.rpc;

import java.lang.annotation.*;

/**
 * 标识一个字段需要注入 RPC 服务代理
 * 
 * @author Network Service Template
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {
    
    /**
     * 服务名称，默认使用字段类型的全限定名
     */
    String name() default "";
    
    /**
     * 服务版本
     */
    String version() default "1.0.0";
    
    /**
     * 服务分组
     */
    String group() default "default";
    
    /**
     * 负载均衡策略
     */
    String loadBalance() default "random";
    
    /**
     * 超时时间 (ms)
     */
    long timeout() default 3000;
}

