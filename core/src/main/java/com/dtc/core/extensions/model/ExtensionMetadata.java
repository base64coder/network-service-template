package com.dtc.core.extensions.model;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * éµâçéå©æé¹?éå­æéµâçé¨å«çéî¿ä¿é­? * 
 * @author Network Service Template
 */
public class ExtensionMetadata {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull String version;
    private final @Nullable String author;
    private final @Nullable String description;
    private final @Nullable String mainClass;
    private final int priority;
    private final int startPriority;

    private ExtensionMetadata(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.version = builder.version;
        this.author = builder.author;
        this.description = builder.description;
        this.mainClass = builder.mainClass;
        this.priority = builder.priority;
        this.startPriority = builder.startPriority;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getMainClass() {
        return mainClass;
    }

    public int getPriority() {
        return priority;
    }

    public int getStartPriority() {
        return startPriority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String version;
        private String author;
        private String description;
        private String mainClass;
        private int priority = 100;
        private int startPriority = 1000;

        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder version(@NotNull String version) {
            this.version = version;
            return this;
        }

        public Builder author(@Nullable String author) {
            this.author = author;
            return this;
        }

        public Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Builder mainClass(@Nullable String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder startPriority(int startPriority) {
            this.startPriority = startPriority;
            return this;
        }

        public ExtensionMetadata build() {
            if (id == null || name == null || version == null) {
                throw new IllegalArgumentException("ID, name, and version are required");
            }
            return new ExtensionMetadata(this);
        }
    }
}
