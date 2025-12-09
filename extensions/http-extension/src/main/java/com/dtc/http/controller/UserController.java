package com.dtc.http.controller;

import com.dtc.http.model.User;
import com.dtc.http.service.UserService;
import com.dtc.annotations.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户相关的 REST API 接口，包括创建、查询、更新和删除用户等功能
 * 
 * @author Network Service Template
 */
@RestController
@RequestMapping("/api/users")
@Singleton
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户
     * 返回系统中所有用户的列表，以 JSON 格式返回
     */
    @GetMapping
    public Map<String, List<User>> getUsers() {
        List<User> users = userService.getAllUsers();
        return Map.of("users", users);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return Map.of(
                "message", "User created successfully",
                "id", created.getId());
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable("id") String id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return Map.of(
                "message", "User updated successfully",
                "id", updated.getId());
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
        return Map.of(
                "message", "User deleted successfully",
                "id", id);
    }
}
