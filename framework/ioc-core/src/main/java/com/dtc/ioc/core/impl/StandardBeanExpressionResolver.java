package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.BeanExpressionContext;
import com.dtc.ioc.core.BeanExpressionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 标准Bean表达式解析器实现
 * 借鉴Spring StandardBeanExpressionResolver的设计
 * 
 * @author Network Service Template
 */
public class StandardBeanExpressionResolver implements BeanExpressionResolver {
    
    private static final Logger log = LoggerFactory.getLogger(StandardBeanExpressionResolver.class);
    
    @Override
    @Nullable
    public Object evaluate(@NotNull String value, @NotNull BeanExpressionContext evalContext) {
        try {
            log.debug("🔧 Evaluating expression: {}", value);
            
            // 简化实现，支持基本的Bean引用
            if (value.startsWith("@") && value.length() > 1) {
                String beanName = value.substring(1);
                return evalContext.getBean(beanName);
            }
            
            // 支持系统属性引用
            if (value.startsWith("${") && value.endsWith("}")) {
                String propertyName = value.substring(2, value.length() - 1);
                return System.getProperty(propertyName);
            }
            
            // 默认返回原值
            return value;
            
        } catch (Exception e) {
            log.error("❌ Error evaluating expression: {}", value, e);
            return null;
        }
    }
}
