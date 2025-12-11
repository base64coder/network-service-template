package com.dtc.framework.persistent.test;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.persistent.transaction.DataSourceTransactionManager;
import com.dtc.framework.persistent.transaction.PlatformTransactionManager;
import com.dtc.framework.persistent.transaction.annotation.Transactional;
import com.dtc.framework.persistent.transaction.interceptor.TransactionAttributeSourceAdvisor;
import com.dtc.framework.persistent.transaction.support.DataSourceUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import com.dtc.framework.beans.annotation.Bean;
import com.dtc.framework.beans.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    public void testCommit() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TxConfig.class);
        ctx.refresh();
        
        UserService userService = ctx.getBean(UserService.class);
        userService.createUser("Alice");
        
        // Verify committed
        DataSource ds = ctx.getBean(DataSource.class);
        try (Connection con = ds.getConnection();
             Statement stmt = con.createStatement()) {
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE name='Alice'");
             rs.next();
             assertEquals(1, rs.getInt(1));
        }
        ctx.close();
    }
    
    @Test
    public void testRollback() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TxConfig.class);
        ctx.refresh();
        
        UserService userService = ctx.getBean(UserService.class);
        try {
            userService.createUserError("Bob");
        } catch (Exception e) {
            System.out.println("Caught expected exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // Verify rolled back
        DataSource ds = ctx.getBean(DataSource.class);
        try (Connection con = ds.getConnection();
             Statement stmt = con.createStatement()) {
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE name='Bob'");
             rs.next();
             assertEquals(0, rs.getInt(1));
        }
        ctx.close();
    }
    
    @Configuration
    public static class TxConfig {
        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            HikariDataSource ds = new HikariDataSource(config);
            
            // Init DB
            try (Connection con = ds.getConnection(); Statement stmt = con.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT, name VARCHAR(255))");
                stmt.execute("DELETE FROM users");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ds;
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
        public UserService userService(DataSource dataSource) {
            return new UserService(dataSource);
        }
    }
    
    public static class UserService {
        private DataSource dataSource;
        
        protected UserService() {} // Required for CGLIB/ByteBuddy proxying
        
        public UserService(DataSource dataSource) { this.dataSource = dataSource; }
        
        @Transactional
        public void createUser(String name) {
            try {
                // Must use DataSourceUtils to get transactional connection
                Connection con = DataSourceUtils.getConnection(dataSource);
                Statement stmt = con.createStatement();
                stmt.execute("INSERT INTO users (name) VALUES ('" + name + "')");
                stmt.close();
                DataSourceUtils.releaseConnection(con, dataSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        @Transactional
        public void createUserError(String name) {
            createUser(name); // Calling self method, AOP proxy might be bypassed if calling this.createUser()
            // But here we call method inside method.
            // If calling this.createUser, it is INTERNAL call, so no proxy interception if we are inside the bean.
            // However, createUserError IS transactional. So the transaction is started here.
            // When createUser is called, it uses the SAME transaction (propagation).
            // So exception here will rollback the transaction started by createUserError.
            // Correct.
            throw new RuntimeException("Rollback");
        }
    }
}

