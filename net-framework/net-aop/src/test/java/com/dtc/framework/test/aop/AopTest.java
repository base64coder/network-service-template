package com.dtc.framework.test.aop;

import com.dtc.framework.aop.MethodBeforeAdvice;
import com.dtc.framework.aop.AfterReturningAdvice;
import com.dtc.framework.aop.aspectj.AspectJExpressionPointcut;
import com.dtc.framework.aop.framework.AdvisedSupport;
import com.dtc.framework.aop.framework.DefaultAopProxyFactory;
import com.dtc.framework.aop.support.DefaultPointcutAdvisor;
import com.dtc.framework.aop.support.SingletonTargetSource;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AopTest {

    @Test
    public void testAop() throws Exception {
        WorldService worldService = new WorldService();
        
        // Setup Advice
        MethodBeforeAdvice before = (method, args, target) -> System.out.println("Before: " + method.getName());
        AfterReturningAdvice after = (returnValue, method, args, target) -> System.out.println("After: " + method.getName());
        
        // Setup Pointcut
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.dtc.framework.test.aop.WorldService.hello(..))");
        
        // Setup AdvisedSupport
        AdvisedSupport advised = new AdvisedSupport();
        advised.setTargetSource(new SingletonTargetSource(worldService));
        advised.addAdvisor(new DefaultPointcutAdvisor(pointcut, before));
        advised.addAdvisor(new DefaultPointcutAdvisor(pointcut, after));
        
        // Create Proxy
        Object proxy = new DefaultAopProxyFactory().createAopProxy(advised).getProxy();
        
        // Invoke
        ((WorldService) proxy).hello();
        
        // Assert class (ByteBuddy subclass)
        assertTrue(proxy.getClass().getName().contains("ByteBuddy"));
    }
}

