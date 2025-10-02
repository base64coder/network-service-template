package com.dtc.api.annotations;

import java.lang.annotation.*;

/**
 * 标记参数、返回值或字段不能为null
 * 
 * @author Network Service Template
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
public @interface NotNull {
}
