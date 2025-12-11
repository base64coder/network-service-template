package com.dtc.framework.test.module;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Inject;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.test.beans.complex.Repository;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderInjectionTest {

    @Test
    public void testProviderInjection() {
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "com.dtc.framework.test.module", 
            "com.dtc.framework.test.beans.complex"
        );
        
        ProviderService service = context.getBean(ProviderService.class);
        assertNotNull(service);
        assertNotNull(service.repositoryProvider);
        
        Repository repo = service.repositoryProvider.get();
        assertNotNull(repo);
        
        // Verify singleton behavior
        Repository repo2 = service.repositoryProvider.get();
        assertSame(repo, repo2);
    }

    @Component
    public static class ProviderService {
        @Inject
        public Provider<Repository> repositoryProvider;
    }
    
    // Using existing Repository class from complex package
}

