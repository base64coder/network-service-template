package com.dtc.framework.test.beans.complex;

import com.dtc.framework.ioc.annotation.Bean;
import com.dtc.framework.ioc.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public Repository repository() {
        return new Repository();
    }
    
    @Bean
    public Service service(Repository repository) {
        return new Service(repository);
    }
}

