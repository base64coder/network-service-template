package com.dtc.framework.web.method.support;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Parameter;

public class ServletRequestResponseMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        return ServletRequest.class.isAssignableFrom(type) ||
               ServletResponse.class.isAssignableFrom(type) ||
               HttpSession.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveArgument(Parameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Class<?> type = parameter.getType();
        if (ServletRequest.class.isAssignableFrom(type)) {
            return request;
        } else if (ServletResponse.class.isAssignableFrom(type)) {
            return response;
        } else if (HttpSession.class.isAssignableFrom(type)) {
            return request.getSession();
        }
        return null;
    }
}

