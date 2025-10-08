package com.dtc.ioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务注解
 * 标识一个类为服务层组件
 * 借鉴Spring的@Service注解
 * 
 * @author Network Service Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Service {
    
    /**
     * Bean名称
     * 如果为空，则使用类名（首字母小写）
     * 
     * @return Bean名称
     */
    String value() default "";
}
