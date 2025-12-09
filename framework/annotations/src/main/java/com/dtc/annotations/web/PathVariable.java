package com.dtc.annotations.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * 璺緞鍙橀噺娉ㄨВ
鐢ㄤ簬缁戝畾URL璺緞涓殑鍙橀噺
鍊熼壌Spring鐨凘PathVariable娉ㄨВ
@author Network Service Template
/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    
    /**
     * 璺緞鍙橀噺鍚嶇О
濡傛灉涓虹┖锛屽垯浣跨敤鍙傛暟鍚嶇О
@return 鍙橀噺鍚嶇О
/
    String value() default "";
    
    /**
     * 鏄惁蹇呴渶
@return 鏄惁蹇呴渶
/
    boolean required() default true;
}

