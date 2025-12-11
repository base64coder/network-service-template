package com.dtc.framework.test.complex;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.context.ApplicationContext;
import com.dtc.framework.test.beans.complex.Repository;
import com.dtc.framework.test.beans.complex.Service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactoryMethodTest {

    @Test
    public void testFactoryMethodInjection() {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.dtc.framework.test.beans.complex");
        
        Repository repo = context.getBean(Repository.class);
        Service service = context.getBean(Service.class);
        
        assertNotNull(repo);
        assertNotNull(service);
        assertSame(repo, service.getRepository());
    }
}

