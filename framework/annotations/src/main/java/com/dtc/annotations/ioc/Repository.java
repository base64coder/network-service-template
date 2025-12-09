package com.dtc.annotations.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仓储注解
 * 标识一个类为数据访问层组件
 * 借鉴 Spring 的 @Repository 注解
 * <p>表示一个类是"仓库"，在领域驱动设计（DDD）中定义为"封装存储、检索和搜索行为的机制，
 * 模拟对象集合"。
 * <p>此注解作为 {@link Component @Component} 的特化，允许通过类路径扫描自动检测实现类。
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Repository {
    
    /**
     * Bean 名称
     * 如果为空，则使用类名（首字母小写）
     * 
     * @return Bean 名称
     */
    String value() default "";
}
