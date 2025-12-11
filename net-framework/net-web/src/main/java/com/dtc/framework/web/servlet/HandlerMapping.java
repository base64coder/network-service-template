package com.dtc.framework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;

public interface HandlerMapping {
    String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";

    Object getHandler(HttpServletRequest request) throws Exception;
}
