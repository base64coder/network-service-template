package com.dtc.framework.persistent.test;

import com.dtc.annotations.persistence.Id;
import com.dtc.annotations.persistence.Table;
import com.dtc.framework.persistent.impl.JdbcRepository;
import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Configuration;
import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.persistent.transaction.DataSourceTransactionManager;
import com.dtc.framework.persistent.transaction.PlatformTransactionManager;
import com.dtc.framework.persistent.transaction.annotation.Transactional;
import com.dtc.framework.persistent.transaction.interceptor.TransactionAttributeSourceAdvisor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcRepositoryTest {

    @Table(value = "users")
    public static class User {
        @Id(keyType = Id.KeyType.AUTO)
        private Long id;
        private String name;
        private Integer age;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    public static class UserRepository extends JdbcRepository<User, Long> {
        public UserRepository(DataSource dataSource) {
            super(dataSource);
        }
    }

    public static class UserService {
        private final UserRepository repo;
        
        protected UserService() { this.repo = null; } // For CGLIB
        
        public UserService(UserRepository repo) { this.repo = repo; }
        
        @Transactional
        public void createUserError(String name) {
            User user = new User();
            user.setName(name);
            repo.save(user);
            throw new RuntimeException("Rollback");
        }
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:test_repo;DB_CLOSE_DELAY=-1");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            HikariDataSource ds = new HikariDataSource(config);

            try (Connection con = ds.getConnection(); Statement stmt = con.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), age INT)");
                stmt.execute("DELETE FROM users"); 
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ds;
        }

        @Bean
        public UserRepository userRepository(DataSource dataSource) {
            return new UserRepository(dataSource);
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public TransactionAttributeSourceAdvisor transactionAdvisor(PlatformTransactionManager transactionManager) {
            return new TransactionAttributeSourceAdvisor(transactionManager);
        }
        
        @Bean
        public UserService userService(UserRepository repo) {
            return new UserService(repo);
        }
    }

    @Test
    public void testCrud() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TestConfig.class);
        ctx.refresh();

        UserRepository repo = ctx.getBean(UserRepository.class);

        // Insert
        User user = new User();
        user.setName("Alice");
        user.setAge(30);
        repo.save(user);
        assertNotNull(user.getId(), "ID should be generated");

        // Select
        User fetched = repo.findById(user.getId());
        assertNotNull(fetched);
        assertEquals("Alice", fetched.getName());

        // Update
        fetched.setAge(31);
        repo.save(fetched);
        
        User updated = repo.findById(user.getId());
        assertEquals(31, updated.getAge());

        // Delete
        repo.delete(updated);
        assertNull(repo.findById(user.getId()));
        
        ctx.close();
    }

    @Test
    public void testTransaction() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TestConfig.class);
        ctx.refresh();

        UserService userService = ctx.getBean(UserService.class);
        UserRepository repo = ctx.getBean(UserRepository.class);

        // Test Rollback
        try {
            userService.createUserError("Bob");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Rollback", e.getMessage());
        }

        List<User> users = repo.findAll();
        boolean hasBob = users.stream().anyMatch(u -> "Bob".equals(u.getName()));
        assertFalse(hasBob, "Bob should be rolled back");
        
        ctx.close();
    }
}
