package com.dtc.http.repository;

import com.dtc.http.model.User;
import com.dtc.annotations.ioc.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户数据访问层
 * 提供用户数据的存储和查询功能
 * 
 * @author Network Service Template
 */
@Repository
@Singleton
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    // 内存存储用户数据
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserRepository() {
        // 初始化示例数据
        save(new User("1", "John Doe", "john@example.com"));
        save(new User("2", "Jane Smith", "jane@example.com"));
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /**
     * 根据ID查询用户
     */
    public User findById(String id) {
        User user = users.get(id);
        if (user == null) {
            // 如果用户不存在，返回一个默认用户
            return new User(id, "John Doe", "john@example.com");
        }
        return user;
    }

    /**
     * 保存用户
     */
    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(String.valueOf(System.currentTimeMillis()));
        }
        users.put(user.getId(), user);
        log.debug("Saved user: {}", user);
        return user;
    }

    /**
     * 根据ID删除用户
     */
    public void deleteById(String id) {
        users.remove(id);
        log.debug("Deleted user with id: {}", id);
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsById(String id) {
        return users.containsKey(id);
    }
}
