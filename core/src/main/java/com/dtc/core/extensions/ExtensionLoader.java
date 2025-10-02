package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.model.ExtensionEvent;
import com.dtc.core.extensions.model.ExtensionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 扩展加载器
 * 负责扫描和加载扩展
 * 
 * @author Network Service Template
 */
public class ExtensionLoader {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLoader.class);
    private static final String EXTENSION_XML_FILE = "extension.xml";
    private static final String DISABLED_FILE = "DISABLED";

    /**
     * 加载扩展目录中的所有扩展
     * 
     * @param extensionsFolder 扩展目录
     * @return 扩展事件列表
     */
    @NotNull
    public Collection<ExtensionEvent> loadExtensions(@NotNull Path extensionsFolder) {
        List<ExtensionEvent> events = new ArrayList<>();

        if (!Files.exists(extensionsFolder)) {
            log.warn("Extensions folder does not exist: {}", extensionsFolder);
            return events;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsFolder)) {
            for (Path extensionFolder : stream) {
                if (Files.isDirectory(extensionFolder)) {
                    ExtensionEvent event = loadSingleExtension(extensionFolder);
                    if (event != null) {
                        events.add(event);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan extensions folder: {}", extensionsFolder, e);
        }

        return events;
    }

    /**
     * 加载单个扩展
     * 
     * @param extensionFolder 扩展文件夹
     * @return 扩展事件
     */
    @Nullable
    public ExtensionEvent loadSingleExtension(@NotNull Path extensionFolder) {
        try {
            // 检查是否被禁用
            boolean disabled = Files.exists(extensionFolder.resolve(DISABLED_FILE));

            // 读取扩展元数据
            ExtensionMetadata metadata = readExtensionMetadata(extensionFolder);
            if (metadata == null) {
                log.warn("Failed to read extension metadata from: {}", extensionFolder);
                return null;
            }

            // 验证扩展文件夹名称与ID是否匹配
            String folderName = extensionFolder.getFileName().toString();
            if (!folderName.equals(metadata.getId())) {
                log.warn("Extension folder name '{}' does not match extension ID '{}'",
                        folderName, metadata.getId());
                return null;
            }

            // 创建扩展事件
            return new ExtensionEvent(
                    disabled ? ExtensionEvent.Change.DISABLE : ExtensionEvent.Change.ENABLE,
                    metadata,
                    extensionFolder);

        } catch (Exception e) {
            log.error("Failed to load extension from: {}", extensionFolder, e);
            return null;
        }
    }

    /**
     * 读取扩展元数据
     * 
     * @param extensionFolder 扩展文件夹
     * @return 扩展元数据
     */
    @Nullable
    private ExtensionMetadata readExtensionMetadata(@NotNull Path extensionFolder) {
        Path xmlFile = extensionFolder.resolve(EXTENSION_XML_FILE);

        if (!Files.exists(xmlFile)) {
            log.warn("Extension XML file not found: {}", xmlFile);
            return null;
        }

        try {
            // 这里应该解析XML文件，为了简化，我们创建一个示例
            return ExtensionMetadata.builder()
                    .id(extensionFolder.getFileName().toString())
                    .name("Sample Extension")
                    .version("1.0.0")
                    .author("Developer")
                    .priority(100)
                    .startPriority(1000)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse extension XML: {}", xmlFile, e);
            return null;
        }
    }
}
