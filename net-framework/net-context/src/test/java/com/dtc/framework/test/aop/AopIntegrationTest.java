package com.dtc.framework.test.aop;

import com.dtc.framework.aop.MethodBeforeAdvice;
import com.dtc.framework.aop.aspectj.AspectJExpressionPointcut;
import com.dtc.framework.aop.support.DefaultPointcutAdvisor;
import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Configuration;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AopIntegrationTest {

    @Test
    public void testPointcutMatching() throws Exception {
        AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
        pc.setExpression("execution(* doSomething(..))");
        
        Method m = IntegrationTestService.class.getMethod("doSomething");
        assertTrue(pc.matches(m, IntegrationTestService.class), "Pointcut should match doSomething");
    }

    @Test
    public void testAopIntegration() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AopConfig.class);
        ctx.refresh();
        
        IntegrationTestService service = ctx.getBean(IntegrationTestService.class);
        service.doSomething();
        
        assertTrue(AopConfig.wasCalled, "Advice should be called");
        // Verify it's a proxy
        assertTrue(service.getClass().getName().contains("ByteBuddy") || java.lang.reflect.Proxy.isProxyClass(service.getClass()));
    }
    
    @Configuration
    public static class AopConfig {
        public static boolean wasCalled = false;
        
        @Bean
        public IntegrationTestService testService() {
            return new IntegrationTestService();
        }

        
        @Bean
        public DefaultPointcutAdvisor advisor() {
            AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
            pointcut.setExpression("execution(* doSomething(..))");
            
            MethodBeforeAdvice advice = (method, args, target) -> {
                System.out.println("Integrated AOP Before");
                wasCalled = true;
            };
            
            return new DefaultPointcutAdvisor(pointcut, advice);
        }
    }

    public static class IntegrationTestService {
        public void doSomething() {
            System.out.println("Doing something");
        }
    }
}
