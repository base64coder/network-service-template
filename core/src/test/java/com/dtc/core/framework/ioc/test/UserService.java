package com.dtc.core.framework.ioc.test;

import com.dtc.core.framework.ioc.annotation.Component;
import com.dtc.core.framework.ioc.annotation.Inject;

@Component
public class UserService {
    @Inject
    public UserRepository userRepository;
}

