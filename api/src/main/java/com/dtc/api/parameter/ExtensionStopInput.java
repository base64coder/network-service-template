package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

/**
 * Extension Stop Input Parameters
 * 
 * @author Network Service Template
 */
public interface ExtensionStopInput {

    /**
     * Get Extension ID
     * 
     * @return Extension ID
     */
    @NotNull
    String getExtensionId();

    /**
     * Get Extension Name
     * 
     * @return Extension Name
     */
    @NotNull
    String getExtensionName();

    /**
     * Get Extension Version
     * 
     * @return Extension Version
     */
    @NotNull
    String getExtensionVersion();

    /**
     * Get Extension Folder Path
     * 
     * @return Extension Folder Path
     */
    @NotNull
    Path getExtensionFolderPath();

    /**
     * Get Server Information
     * 
     * @return Server Information
     */
    @NotNull
    ServerInformation getServerInformation();

    /**
     * Get Configuration Parameters
     * 
     * @return Configuration Parameter Map
     */
    @NotNull
    Map<String, String> getConfiguration();
}
