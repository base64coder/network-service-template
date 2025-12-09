package com.dtc.annotations.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
     * ååºä½æ³¨è§£
æ è¯æ¹æ³è¿åå¼åºè¯¥ç´æ¥åå¥HTTPååºä½ï¼èä¸æ¯ä½ä¸ºè§å¾åç§°
åé´Springç@ResponseBodyæ³¨è§£
<p>å½æ¹æ³è¿åæ®éå¯¹è±¡ï¼éHttpResponseExï¼æ¶ï¼æ¡æ¶ä¼èªå¨å°å¶è½¬æ¢ä¸ºJSONååºã
å¦ææ¹æ³å·²ç»è¿åHttpResponseExï¼åæ­¤æ³¨è§£å¯çç¥ã
@author Network Service Template
/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}

