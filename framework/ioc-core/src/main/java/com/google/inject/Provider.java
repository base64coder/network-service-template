package com.google.inject;

/**
 * Compatibility interface for com.google.inject.Provider.
 * Extends our internal Provider to ensure type safety.
 */
public interface Provider<T> extends com.dtc.ioc.core.provider.Provider<T> {
}

