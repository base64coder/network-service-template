package com.dtc.annotations.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务注解
 * 标识一个类为服务层组件
 * 借鉴 Spring 的 @Service 注解
 * <p>表示一个类是"服务"，在领域驱动设计（DDD）中定义为"作为接口提供的操作，
 * 在模型中独立存在，没有封装状态"。
 * <p>此注解作为 {@link Component @Component} 的特化，允许通过类路径扫描自动检测实现类。
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    
    /**
     * Bean 名称
     * 如果为空，则使用类名（首字母小写）
     * 
     * @return Bean 名称
     */
    String value() default "";
}
