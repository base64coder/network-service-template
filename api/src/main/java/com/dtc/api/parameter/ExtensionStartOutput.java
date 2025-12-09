package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.util.Optional;

/**
 * Extension Start Output Parameters
 * 
 * @author Network Service Template
 */
public interface ExtensionStartOutput {

    /**
     * Set Startup Failure Reason
     * 
     * @param reason Failure Reason
     */
    void preventStartup(@NotNull String reason);

    /**
     * Get Startup Failure Reason
     * 
     * @return Failure Reason, returns empty if startup succeeded
     */
    @NotNull
    Optional<String> getReason();

    /**
     * Set Extension Configuration
     * 
     * @param key   Configuration Key
     * @param value Configuration Value
     */
    void setConfiguration(@NotNull String key, @NotNull String value);

    /**
     * Get Extension Configuration
     * 
     * @param key Configuration Key
     * @return Configuration Value
     */
    @Nullable
    String getConfiguration(@NotNull String key);
}
