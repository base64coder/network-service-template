package com.dtc.core.web;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRouteManager;
import com.dtc.core.network.http.route.HttpRouteHandler;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;
import com.dtc.core.web.argument.HandlerMethodArgumentResolverComposite;
import com.dtc.annotations.web.DeleteMapping;
import com.dtc.annotations.web.GetMapping;
import com.dtc.annotations.web.PostMapping;
import com.dtc.annotations.web.PutMapping;
import com.dtc.annotations.web.RequestMapping;
import com.dtc.annotations.web.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Web控制器扫描器
 * 扫描并注册所有@RestController注解的控制器
 * 参考Spring的RequestMappingHandlerMapping实现
 * 
 * @author Network Service Template
 */
@Singleton
public class WebControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(WebControllerScanner.class);

    private final @NotNull HttpRouteManager routeManager;
    private final @NotNull BeanProvider beanProvider;
    private final @NotNull HandlerMethodArgumentResolverComposite argumentResolver;
    private final @NotNull Map<String, HandlerMethod> handlerMethods = new ConcurrentHashMap<>();

    @Inject
    public WebControllerScanner(@NotNull HttpRouteManager routeManager,
                                @NotNull BeanProvider beanProvider,
                                @NotNull HandlerMethodArgumentResolverComposite argumentResolver) {
        this.routeManager = routeManager;
        this.beanProvider = beanProvider;
        this.argumentResolver = argumentResolver;
    }

    /**
     * 扫描并注册所有控制器
     * 
     * @param basePackage 基础包名，如果为空则扫描所有已注册的Bean
     */
    public void scanAndRegister(@NotNull String basePackage) {
        log.info("🔍 Scanning for controllers in package: {}", basePackage);

        try {
            // 获取所有Bean
            Map<String, Object> beans = beanProvider.getAllBeans();
            
            int controllerCount = 0;
            int methodCount = 0;

            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object bean = entry.getValue();
                if (bean == null) {
                    continue;
                }
                
                Class<?> beanType = bean.getClass();

                // 检查是否有@RestController注解
                if (beanType.isAnnotationPresent(RestController.class)) {
                    controllerCount++;
                    methodCount += registerController(bean, beanType);
                }
            }

            if (controllerCount > 0) {
                log.info("✅ Scanned and registered {} controllers with {} handler methods", controllerCount, methodCount);
            } else {
                log.info("ℹ️  No @RestController found in package: {}", basePackage);
            }

        } catch (Exception e) {
            log.error("❌ Failed to scan controllers", e);
            throw new RuntimeException("Failed to scan controllers", e);
        }
    }

    /**
     * 扫描并注册指定类型的控制器
     * 如果指定类型则只注册该控制器，否则扫描所有已注册的Bean
     * 
     * @param controllerClass 控制器类型
     * @param controllerInstance 控制器实例
     */
    public void registerController(@NotNull Class<?> controllerClass, @NotNull Object controllerInstance) {
        if (controllerClass.isAnnotationPresent(RestController.class)) {
            registerController(controllerInstance, controllerClass);
            log.info("✅ Registered controller: {}", controllerClass.getSimpleName());
        } else {
            log.warn("⚠️  Class {} is not annotated with @RestController", controllerClass.getName());
        }
    }

    /**
     * 注册控制器
     */
    private int registerController(@NotNull Object bean, @NotNull Class<?> controllerType) {
        RestController controllerAnnotation = controllerType.getAnnotation(RestController.class);
        String basePath = controllerAnnotation != null ? controllerAnnotation.value() : "";

        int methodCount = 0;
        Method[] methods = controllerType.getDeclaredMethods();

        for (Method method : methods) {
            // 检查方法是否有映射注解
            RequestMappingInfo mappingInfo = extractMappingInfo(method, basePath);
            if (mappingInfo != null) {
                registerHandlerMethod(bean, method, mappingInfo);
                methodCount++;
            }
        }

        return methodCount;
    }

    /**
     * 提取映射信息
     */
    @Nullable
    private RequestMappingInfo extractMappingInfo(@NotNull Method method, @NotNull String basePath) {
        // 检查@GetMapping
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            String path = basePath + mapping.value();
            return new RequestMappingInfo("GET", path);
        }

        // 检查@PostMapping
        if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            String path = basePath + mapping.value();
            return new RequestMappingInfo("POST", path);
        }

        // 检查@PutMapping
        if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            String path = basePath + mapping.value();
            return new RequestMappingInfo("PUT", path);
        }

        // 检查@DeleteMapping
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            String path = basePath + mapping.value();
            return new RequestMappingInfo("DELETE", path);
        }

        // 检查@RequestMapping
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            String path = basePath + mapping.value();
            RequestMapping.RequestMethod[] methods = mapping.method();
            if (methods.length > 0) {
                return new RequestMappingInfo(methods[0].name(), path);
            } else {
                return new RequestMappingInfo("GET", path);
            }
        }

        return null;
    }

    /**
     * 注册处理方法
     */
    private void registerHandlerMethod(@NotNull Object bean, @NotNull Method method, @NotNull RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        String routeKey = mappingInfo.getMethod() + ":" + mappingInfo.getPath();
        handlerMethods.put(routeKey, handlerMethod);

        // 创建路由处理器
        HttpRouteHandler routeHandler = createRouteHandler(handlerMethod);

        // 注册路由
        routeManager.registerRoute(mappingInfo.getMethod(), mappingInfo.getPath(), routeHandler);

        log.debug("✅ Registered handler method: {} {} -> {}", 
            mappingInfo.getMethod(), mappingInfo.getPath(), handlerMethod.getDescription());
    }

    /**
     * 创建路由处理器
     */
    @NotNull
    private HttpRouteHandler createRouteHandler(@NotNull HandlerMethod handlerMethod) {
        return request -> {
            try {
                // 解析方法参数
                Object[] args = resolveArguments(handlerMethod, request);

                // 调用处理方法
                Object result = handlerMethod.getMethod().invoke(handlerMethod.getBean(), args);

                // 处理返回值
                return handleReturnValue(result, request);

            } catch (Exception e) {
                log.error("❌ Error invoking handler method: {}", handlerMethod.getDescription(), e);
                return createErrorResponse(500, "Internal server error: " + e.getMessage());
            }
        };
    }

    /**
     * 解析方法参数
     */
    @NotNull
    private Object[] resolveArguments(@NotNull HandlerMethod handlerMethod, @NotNull HttpRequestEx request) throws Exception {
        Parameter[] parameters = handlerMethod.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // 直接注入HttpRequestEx类型
            if (parameter.getType() == HttpRequestEx.class) {
                args[i] = request;
            } else if (argumentResolver.supportsParameter(parameter)) {
                args[i] = argumentResolver.resolveArgument(parameter, request);
            } else {
                args[i] = null;
            }
        }

        return args;
    }

    /**
     * 处理返回值
     */
    @NotNull
    private HttpResponseEx handleReturnValue(@Nullable Object returnValue, @NotNull HttpRequestEx request) {
        if (returnValue == null) {
            return createSuccessResponse(200, null);
        }

        // 如果返回值是HttpResponseEx类型，直接返回
        if (returnValue instanceof HttpResponseEx) {
            return (HttpResponseEx) returnValue;
        }

        // 否则序列化为JSON响应
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = objectMapper.writeValueAsString(returnValue);
            return createSuccessResponse(200, json);
        } catch (Exception e) {
            log.error("Failed to serialize return value", e);
            return createErrorResponse(500, "Failed to serialize response");
        }
    }

    /**
     * 创建成功响应
     */
    @NotNull
    private HttpResponseEx createSuccessResponse(int statusCode, @Nullable String body) {
        return new HttpResponseEx.Builder()
            .statusCode(statusCode)
            .body(body != null ? body : "")
            .contentType("application/json")
            .build();
    }

    /**
     * 创建错误响应
     */
    @NotNull
    private HttpResponseEx createErrorResponse(int statusCode, @NotNull String message) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = objectMapper.writeValueAsString(Map.of("error", message));
            return new HttpResponseEx.Builder()
                .statusCode(statusCode)
                .body(json)
                .contentType("application/json")
                .build();
        } catch (Exception e) {
            return new HttpResponseEx.Builder()
                .statusCode(statusCode)
                .body("{\"error\":\"" + message + "\"}")
                .contentType("application/json")
                .build();
        }
    }

    /**
     * 请求映射信息
     */
    private static class RequestMappingInfo {
        private final String method;
        private final String path;

        public RequestMappingInfo(@NotNull String method, @NotNull String path) {
            this.method = method;
            this.path = path;
        }

        @NotNull
        public String getMethod() {
            return method;
        }

        @NotNull
        public String getPath() {
            return path;
        }
    }
}
