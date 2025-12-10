package com.dtc.annotations.condition;

import java.util.Map;

/**
 * 条件接口
 * 用于条件判断
 * 
 * @author Network Service Template
 */
public interface Condition {
    /**
     * 判断条件是否匹配
     * @param context 条件上下文
     * @param metadata 元数据
     * @return 是否匹配
     */
    boolean matches(ConditionContext context, Map<String, Object> metadata);
}
