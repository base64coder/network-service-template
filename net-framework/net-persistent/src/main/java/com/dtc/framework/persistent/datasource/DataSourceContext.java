package com.dtc.framework.persistent.datasource;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * 数据源上下文
 * 用于在当前线程中存储和获取数据源类型（Master/Slave）
 */
public class DataSourceContext {

    public enum Type {
        MASTER,
        SLAVE
    }

    private static final ThreadLocal<Type> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前数据源类型
     */
    public static void set(@NotNull Type type) {
        CONTEXT.set(type);
    }

    /**
     * 获取当前数据源类型
     * 默认为 MASTER
     */
    @NotNull
    public static Type get() {
        Type type = CONTEXT.get();
        return type == null ? Type.MASTER : type;
    }

    /**
     * 清除当前数据源类型
     */
    public static void clear() {
        CONTEXT.remove();
    }
}

