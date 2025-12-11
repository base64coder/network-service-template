package com.dtc.core.persistence.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Connection Proxy
 * Prevents closing the connection during transaction
 */
public class ConnectionProxy implements InvocationHandler {
    
    private final Connection connection;
    
    private ConnectionProxy(Connection connection) {
        this.connection = connection;
    }
    
    public static Connection wrap(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
            ConnectionProxy.class.getClassLoader(),
            new Class<?>[] { Connection.class },
            new ConnectionProxy(connection)
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("close".equals(method.getName())) {
            // Do nothing on close
            return null;
        }
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        if ("hashCode".equals(method.getName())) {
            return System.identityHashCode(proxy);
        }
        if ("toString".equals(method.getName())) {
            return "ConnectionProxy(" + connection.toString() + ")";
        }
        if ("unwrap".equals(method.getName())) {
             return ((Class<?>) args[0]).isInstance(proxy) ? proxy : connection.unwrap((Class<?>) args[0]);
        }
        if ("isWrapperFor".equals(method.getName())) {
             return ((Class<?>) args[0]).isInstance(proxy) || connection.isWrapperFor((Class<?>) args[0]);
        }
        
        return method.invoke(connection, args);
    }
}

