package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.ConditionalOnClass;

@Component
@ConditionalOnClass("java.lang.String")
public class ConditionalBean {}

