package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.BeanExpressionContext;
import com.dtc.ioc.core.BeanExpressionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
     * æ åBeanè¡¨è¾¾å¼è§£æå¨å®ç°
åé´Spring StandardBeanExpressionResolverçè®¾è®¡
@author Network Service Template
/
public class StandardBeanExpressionResolver implements BeanExpressionResolver {
    
    private static final Logger log = LoggerFactory.getLogger(StandardBeanExpressionResolver.class);
    
    @Override
    @Nullable
    public Object evaluate(@NotNull String value, @NotNull BeanExpressionContext evalContext) {
        try {
            log.debug("ð§ Evaluating expression: {}", value);
            
            // ç®åå®ç°ï¼æ¯æåºæ¬çBeanå¼ç¨
            if (value.startsWith("@") && value.length() > 1) {
                String beanName = value.substring(1);
                return evalContext.getBean(beanName);
            }
            
            // æ¯æç³»ç»å±æ§å¼ç¨
            if (value.startsWith("${") && value.endsWith("}")) {
                String propertyName = value.substring(2, value.length() - 1);
                return System.getProperty(propertyName);
            }
            
            // é»è®¤è¿ååå¼
            return value;
            
        } catch (Exception e) {
            log.error("â Error evaluating expression: {}", value, e);
            return null;
        }
    }
}
