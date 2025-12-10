package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * Bean表达式解析器接口
 * 解析Bean表达式
 * 
 * @author Network Service Template
 */
public interface BeanExpressionResolver {
    
    /**
     * 解析表达式
     * @param value 表达式值
     * @param evalContext 评估上下文
     * @return 解析结果
     */
    @Nullable
    Object evaluate(@NotNull String value, @NotNull BeanExpressionContext evalContext);
}
