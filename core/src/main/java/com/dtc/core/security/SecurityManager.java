package com.dtc.core.security;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ç¹å¤åç» ï¼æé£? * ç» ï¼æç¹å¤åé©ç¨¿å§é¨å«å§é³? * 
 * @author Network Service Template
 */
@Singleton
public class SecurityManager {

    private static final Logger log = LoggerFactory.getLogger(SecurityManager.class);

    private final @NotNull AuthenticationService authenticationService;
    private final @NotNull AuthorizationService authorizationService;

    @Inject
    public SecurityManager(
            @NotNull AuthenticationService authenticationService,
            @NotNull AuthorizationService authorizationService) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;

        log.info("Security manager initialized");
    }

    /**
     * é¾å³°å½çãçéå¶å§
     * 
     * @return çãçéå¶å§
     */
    @NotNull
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * é¾å³°å½éºå æ½éå¶å§
     * 
     * @return éºå æ½éå¶å§
     */
    @NotNull
    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }
}
