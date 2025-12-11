package com.dtc.framework.test.beans.basic;

import com.dtc.framework.ioc.annotation.Component;
import com.dtc.framework.ioc.annotation.Inject;

@Component
public class UserService {
    @Inject
    public UserRepository userRepository;
}

