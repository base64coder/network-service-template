package com.dtc.core.network.http.route;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;

/**
 * HTTP çºîæ±æ¾¶å­æé£ã¦å¸´é?ç¹æ°«ç®æ¾¶å­æ HTTP çéç°é¨å¬æå¨? * 
 * @author Network Service Template
 */
@FunctionalInterface
public interface HttpRouteHandler {

    /**
     * æ¾¶å­æ HTTP çéç°
     * 
     * @param request HTTP çéç°
     * @return HTTP éå¶ç°²
     */
    @NotNull
    HttpResponseEx handle(@NotNull HttpRequestEx request);
}
