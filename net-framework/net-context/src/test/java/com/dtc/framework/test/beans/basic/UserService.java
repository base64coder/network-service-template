package com.dtc.framework.test.beans.basic;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Inject;

@Component
public class UserService {
    @Inject
    public UserRepository userRepository;
}

