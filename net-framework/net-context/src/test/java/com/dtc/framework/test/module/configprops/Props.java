package com.dtc.framework.test.module.configprops;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config")
@Component
public class Props {
    public String name;
    public int timeout;
}

