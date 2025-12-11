package com.dtc.framework.test.module;

import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Configuration;
import com.dtc.framework.beans.annotation.Profile;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.env.AbstractEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileTest {

    @Test
    public void testProfile() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ((AbstractEnvironment) ctx.getEnvironment()).addActiveProfile("dev");
        ctx.register(ProfileConfig.class);
        ctx.refresh();
        
        assertTrue(ctx.containsBean("devBean"));
        assertFalse(ctx.containsBean("prodBean"));
    }
    
    @Configuration
    public static class ProfileConfig {
        @Bean("devBean")
        @Profile("dev")
        public String dev() { return "dev"; }
        
        @Bean("prodBean")
        @Profile("prod")
        public String prod() { return "prod"; }
    }
}

