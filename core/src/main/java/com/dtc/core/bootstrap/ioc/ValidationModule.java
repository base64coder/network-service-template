package com.dtc.core.bootstrap.ioc;

import com.dtc.core.validation.ValidationInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 验证模块
 * 配置注解验证拦截器
 * 
 * @author Network Service Template
 */
public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        // 创建验证拦截器
        MethodInterceptor validationInterceptor = new ValidationInterceptor();

        // 配置拦截器
        // 拦截所有带有 @NotNull 或 @Nullable 注解的方法
        bindInterceptor(
                Matchers.any(), // 任何类
                Matchers.any(), // 任何方法
                validationInterceptor);

        // 也可以针对特定包进行拦截
        // bindInterceptor(
        // Matchers.inSubpackage("com.dtc.core"),
        // Matchers.any(),
        // validationInterceptor
        // );
    }
}
