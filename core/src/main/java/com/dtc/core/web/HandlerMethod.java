package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 处理方法封装类
 * 封装Bean实例和方法，提供方法参数和返回值信息
 * 参考Spring的HandlerMethod实现
 * 
 * @author Network Service Template
 */
public class HandlerMethod {

    private static final Logger log = LoggerFactory.getLogger(HandlerMethod.class);

    private final @NotNull Object bean;
    private final @NotNull Method method;
    private final @NotNull Class<?> beanType;
    private final @NotNull String description;

    /**
     * 通过Bean实例和方法创建HandlerMethod
     */
    public HandlerMethod(@NotNull Object bean, @NotNull Method method) {
        this.bean = bean;
        this.method = method;
        this.beanType = bean.getClass();
        this.description = initDescription(beanType, method);
    }

    /**
     * 获取Bean实例
     */
    @NotNull
    public Object getBean() {
        return bean;
    }

    /**
     * 获取方法
     */
    @NotNull
    public Method getMethod() {
        return method;
    }

    /**
     * 获取Bean类型
     */
    @NotNull
    public Class<?> getBeanType() {
        return beanType;
    }

    /**
     * 获取方法描述信息
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * 初始化方法描述信息
     */
    @NotNull
    private String initDescription(@NotNull Class<?> beanType, @NotNull Method method) {
        return String.format("%s#%s(%s)", 
            beanType.getSimpleName(), 
            method.getName(),
            Arrays.toString(method.getParameterTypes()));
    }

    /**
     * 获取方法参数数组
     */
    @NotNull
    public java.lang.reflect.Parameter[] getParameters() {
        return method.getParameters();
    }

    /**
     * 获取方法返回类型
     */
    @NotNull
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HandlerMethod that = (HandlerMethod) obj;
        return bean.equals(that.bean) && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return bean.hashCode() * 31 + method.hashCode();
    }

    @Override
    public String toString() {
        return description;
    }
}
