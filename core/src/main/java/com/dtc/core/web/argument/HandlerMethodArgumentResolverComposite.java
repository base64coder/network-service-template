package com.dtc.core.web.argument;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.web.HandlerMethodArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 参数解析器组合类
 * 负责管理多个参数解析器，并按顺序尝试解析
 * 参考Spring的HandlerMethodArgumentResolverComposite实现
 * 
 * @author Network Service Template
 */
@Singleton
public class HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(HandlerMethodArgumentResolverComposite.class);

    private final List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

    @Inject
    public HandlerMethodArgumentResolverComposite() {
        // 注册默认的参数解析器
        addResolver(new PathVariableMethodArgumentResolver());
        addResolver(new RequestParamMethodArgumentResolver());
        addResolver(new RequestBodyMethodArgumentResolver());
    }

    /**
     * 添加参数解析器
     */
    public void addResolver(@NotNull HandlerMethodArgumentResolver resolver) {
        resolvers.add(resolver);
        log.debug("Added argument resolver: {}", resolver.getClass().getSimpleName());
    }

    /**
     * 添加参数解析器到指定位置
     */
    public void addResolver(int index, @NotNull HandlerMethodArgumentResolver resolver) {
        resolvers.add(index, resolver);
        log.debug("Added argument resolver at index {}: {}", index, resolver.getClass().getSimpleName());
    }

    @Override
    public boolean supportsParameter(@NotNull Parameter parameter) {
        return getResolver(parameter) != null;
    }

    @Override
    @Nullable
    public Object resolveArgument(@NotNull Parameter parameter, @NotNull HttpRequestEx request) throws Exception {
        HandlerMethodArgumentResolver resolver = getResolver(parameter);
        if (resolver == null) {
            throw new IllegalArgumentException("No suitable resolver for parameter: " + parameter.getName());
        }
        return resolver.resolveArgument(parameter, request);
    }

    /**
     * 获取支持该参数的解析器
     */
    @Nullable
    private HandlerMethodArgumentResolver getResolver(@NotNull Parameter parameter) {
        for (HandlerMethodArgumentResolver resolver : resolvers) {
            if (resolver.supportsParameter(parameter)) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * 获取所有解析器
     */
    @NotNull
    public List<HandlerMethodArgumentResolver> getResolvers() {
        return new ArrayList<>(resolvers);
    }
}
