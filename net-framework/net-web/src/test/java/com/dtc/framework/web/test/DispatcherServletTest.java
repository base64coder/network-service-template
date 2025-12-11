package com.dtc.framework.web.test;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.web.bind.annotation.GetMapping;
import com.dtc.framework.web.bind.annotation.PostMapping;
import com.dtc.framework.web.bind.annotation.RestController;
import com.dtc.framework.web.bind.annotation.ResponseBody;
import com.dtc.framework.web.bind.annotation.RequestBody;
import com.dtc.framework.web.bind.annotation.RequestParam;
import com.dtc.framework.web.bind.annotation.PathVariable;
import com.dtc.framework.web.servlet.DispatcherServlet;
import com.dtc.framework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import com.dtc.framework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import com.dtc.framework.web.servlet.HandlerInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatcherServletTest {

    @Test
    public void testDispatch() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RequestMappingHandlerMapping.class);
        context.register(RequestMappingHandlerAdapter.class);
        context.register(HelloController.class);
        context.register(TestInterceptor.class); // Register Interceptor
        context.refresh();
        
        DispatcherServlet servlet = new DispatcherServlet(context);
        servlet.init();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/hello");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write(int b) { baos.write(b); }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        };
        when(response.getOutputStream()).thenReturn(sos);
        
        servlet.service(request, response);
        
        String result = baos.toString();
        if (!result.contains("Hello")) {
            throw new RuntimeException("Expected response to contain Hello, got: " + result);
        }
        
        TestInterceptor interceptor = context.getBean(TestInterceptor.class);
        assertTrue(interceptor.preHandleCalled, "Interceptor preHandle should be called");
    }
    
    @Test
    public void testArgumentResolution() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RequestMappingHandlerMapping.class);
        context.register(RequestMappingHandlerAdapter.class);
        context.register(HelloController.class);
        context.refresh();
        
        DispatcherServlet servlet = new DispatcherServlet(context);
        servlet.init();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/greet");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("name")).thenReturn("World");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write(int b) { baos.write(b); }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        };
        when(response.getOutputStream()).thenReturn(sos);
        
        servlet.service(request, response);
        
        String result = baos.toString();
        if (!result.contains("Hello, World")) {
            throw new RuntimeException("Expected response to contain Hello, World, got: " + result);
        }
    }

    @Test
    public void testRequestBody() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RequestMappingHandlerMapping.class);
        context.register(RequestMappingHandlerAdapter.class);
        context.register(HelloController.class);
        context.refresh();
        
        DispatcherServlet servlet = new DispatcherServlet(context);
        servlet.init();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/echo");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");
        
        String json = "{\"name\":\"John\"}";
        jakarta.servlet.ServletInputStream sis = new jakarta.servlet.ServletInputStream() {
            private final java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(json.getBytes());
            @Override public int read() { return bais.read(); }
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(jakarta.servlet.ReadListener readListener) {}
        };
        when(request.getInputStream()).thenReturn(sis);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write(int b) { baos.write(b); }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        };
        when(response.getOutputStream()).thenReturn(sos);
        
        servlet.service(request, response);
        
        String result = baos.toString();
        if (!result.contains("\"name\":\"John\"")) {
             throw new RuntimeException("Expected JSON response with John, got: " + result);
        }
    }

import java.util.HashMap;
import java.util.Map;

// ... imports ...

    @Test
    public void testPathVariable() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RequestMappingHandlerMapping.class);
        context.register(RequestMappingHandlerAdapter.class);
        context.register(HelloController.class);
        context.refresh();
        
        DispatcherServlet servlet = new DispatcherServlet(context);
        servlet.init();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        // Handle attributes for PathVariable
        final Map<String, Object> attributes = new HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            attributes.put(key, value);
            return null;
        }).when(request).setAttribute(anyString(), any());
        
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return attributes.get(key);
        }).when(request).getAttribute(anyString());
        
        when(request.getRequestURI()).thenReturn("/users/123");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write(int b) { baos.write(b); }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        };
        when(response.getOutputStream()).thenReturn(sos);
        
        servlet.service(request, response);
        
        String result = baos.toString();
        if (!result.contains("User: 123")) {
             throw new RuntimeException("Expected response to contain User: 123, got: " + result);
        }
    }

    @RestController
    public static class HelloController {
        @GetMapping("/hello")
        @ResponseBody
        public String hello() {
            return "Hello";
        }

        @GetMapping("/greet")
        @ResponseBody
        public String greet(@RequestParam("name") String name) {
            return "Hello, " + name;
        }
        
        @PostMapping("/echo")
        @ResponseBody
        public User echo(@RequestBody User user) {
            return user;
        }
        
        @GetMapping("/users/{id}")
        @ResponseBody
        public String getUser(@PathVariable("id") int id) {
            return "User: " + id;
        }
    }
    
    public static class User {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class TestInterceptor implements HandlerInterceptor {
        public boolean preHandleCalled = false;
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            preHandleCalled = true;
            return true;
        }
    }
}

