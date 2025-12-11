package com.dtc.framework.web.bind.annotation;

import com.dtc.framework.beans.annotation.Component;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ResponseBody
public @interface RestController {
    String value() default "";
}

