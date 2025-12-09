package com.dtc.framework.beans.provider;

/**
 * Provider interface, similar to javax.inject.Provider or com.google.inject.Provider
 * Provides an instance of T
 *
 * @param <T> the type of object this provides
 */
@FunctionalInterface
public interface Provider<T> {
    
    /**
     * Provides an instance of T.
     *
     * @return an instance of T
     */
    T get();
}

