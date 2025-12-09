package com.dtc.core.extensions.model;

import com.dtc.api.annotations.NotNull;
import java.nio.file.Path;

/**
 * 扩展事件
 * 表示扩展状态变化的事件
 * 
 * @author Network Service Template
 */
public class ExtensionEvent {

    public enum Change {
        ENABLE, // 启用扩展
        DISABLE, // 禁用扩展
        UPDATE, // 更新扩展
        REMOVE // 移除扩展
    }

    private final @NotNull Change change;
    private final @NotNull ExtensionMetadata metadata;
    private final @NotNull Path extensionFolder;
    private final boolean embedded;

    public ExtensionEvent(@NotNull Change change, @NotNull ExtensionMetadata metadata, @NotNull Path extensionFolder) {
        this(change, metadata, extensionFolder, false);
    }

    public ExtensionEvent(@NotNull Change change, @NotNull ExtensionMetadata metadata, @NotNull Path extensionFolder,
            boolean embedded) {
        this.change = change;
        this.metadata = metadata;
        this.extensionFolder = extensionFolder;
        this.embedded = embedded;
    }

    @NotNull
    public Change getChange() {
        return change;
    }

    @NotNull
    public ExtensionMetadata getMetadata() {
        return metadata;
    }

    @NotNull
    public Path getExtensionFolder() {
        return extensionFolder;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    @NotNull
    public String getExtensionId() {
        return metadata.getId();
    }

    @Override
    public String toString() {
        return String.format("ExtensionEvent{change=%s, id=%s, version=%s, embedded=%s}", change, metadata.getId(),
                metadata.getVersion(), embedded);
    }
}
