package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;

/**
 * 签名接口
 * 表示连接点的签名信息
 * 
 * @author Network Service Template
 */
public interface Signature {
    
    /**
     * 获取声明类
     * @return 声明类
     */
    @NotNull
    Class<?> getDeclaringType();
    
    /**
     * 获取签名名称
     * @return 签名名称
     */
    @NotNull
    String getName();
    
    /**
     * 获取修饰符
     * @return 修饰符
     */
    int getModifiers();
    
    /**
     * 转换为字符串
     * @return 字符串表示
     */
    @NotNull
    String toString();
}

