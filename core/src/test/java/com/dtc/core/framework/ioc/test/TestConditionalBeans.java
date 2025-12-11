package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.ConditionalOnClass;
import com.dtc.core.framework.ioc.annotation.ConditionalOnMissingBean;

@Component
@ConditionalOnClass("java.lang.String")
class ConditionalBean {}

@Component
class PrimaryBean {}

@Component
@ConditionalOnMissingBean(classes = {PrimaryBean.class})
class ConditionalOnMissingBeanBean {}

