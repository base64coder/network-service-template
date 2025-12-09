package com.dtc.core.bootstrap.ioc;

import com.dtc.core.validation.ValidationInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 验证模块
 * 配置验证拦截器
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
                Matchers.any(), // 所有类型
                Matchers.any(), // 所有方法
                validationInterceptor);

        // 可以限制拦截范围，例如只拦截特定包下的方法
        // bindInterceptor(
        // Matchers.inSubpackage("com.dtc.core"),
        // Matchers.any(),
        // validationInterceptor
        // );
    }
}
