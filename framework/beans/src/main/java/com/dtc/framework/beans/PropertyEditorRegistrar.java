package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import java.beans.PropertyEditor;

/**
     * å±æ§ç¼è¾å¨æ³¨åå¨æ¥å£
æ³¨åå±æ§ç¼è¾å¨
åé´Spring PropertyEditorRegistrarçè®¾è®¡
@author Network Service Template
/
public interface PropertyEditorRegistrar {
    
    /**
     * æ³¨åå±æ§ç¼è¾å¨
@param registry å±æ§ç¼è¾å¨æ³¨åè¡¨
/
    void registerCustomEditors(@NotNull PropertyEditorRegistry registry);
}
