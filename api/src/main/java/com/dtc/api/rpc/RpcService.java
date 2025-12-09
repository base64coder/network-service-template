package com.dtc.api.rpc;

import java.lang.annotation.*;

/**
 * 标识一个类为 RPC 服务提供者
 * 框架会自动将其注册到服务注册中心
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcService {
    
    /**
     * 服务名称，默认使用接口的全限定名
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
     * 权重
     */
    int weight() default 100;
}

