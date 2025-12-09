package com.dtc.http.service;

import com.dtc.http.model.User;
import com.dtc.http.repository.UserRepository;
import com.dtc.annotations.ioc.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 用户服务
 * 处理用户相关的业务逻辑
 * 
 * @author Network Service Template
 */
@Service
@Singleton
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll();
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(String id) {
        log.debug("Getting user with id: {}", id);
        return userRepository.findById(id);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        log.info("Creating user: {}", user);
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    public User updateUser(String id, User user) {
        log.info("Updating user {}: {}", id, user);
        user.setId(id);
        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(String id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }
}
