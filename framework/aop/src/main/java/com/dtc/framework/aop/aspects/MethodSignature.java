package com.dtc.framework.aop.aspects;

import com.dtc.api.annotations.NotNull;
import java.lang.reflect.Method;

/**
 * 方法签名接口
 * 表示方法连接点的签名信息
 * 
 * @author Network Service Template
 */
public interface MethodSignature extends Signature {
    
    /**
     * 获取方法
     * @return 方法对象
     */
    @NotNull
    Method getMethod();
    
    /**
     * 获取返回类型
     * @return 返回类型
     */
    @NotNull
    Class<?> getReturnType();
    
    /**
     * 获取参数类型
     * @return 参数类型数组
     */
    @NotNull
    Class<?>[] getParameterTypes();
}

