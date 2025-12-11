package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.ConditionalOnMissingBean;

@Component
@ConditionalOnMissingBean(value = {PrimaryBean.class})
public class ConditionalOnMissingBeanBean {}

