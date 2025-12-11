package com.dtc.framework.test.beans.complex;

import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Configuration;

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

