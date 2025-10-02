package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.model.ExtensionEvent;
import com.dtc.core.extensions.model.ExtensionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 扩展加载器 负责扫描和加载扩展
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
                log.warn("Extension folder name '{}' does not match extension ID '{}'", folderName, metadata.getId());
                return null;
            }

            // 创建扩展事件
            return new ExtensionEvent(disabled ? ExtensionEvent.Change.DISABLE : ExtensionEvent.Change.ENABLE, metadata,
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
            // 解析XML文件
            return parseExtensionXml(xmlFile);
        } catch (Exception e) {
            log.error("Failed to parse extension XML: {}", xmlFile, e);
            return null;
        }
    }

    /**
     * 解析扩展XML文件
     * 
     * @param xmlFile XML文件路径
     * @return 扩展元数据
     */
    @Nullable
    private ExtensionMetadata parseExtensionXml(@NotNull Path xmlFile) {
        try {
            // 简单的XML解析实现
            String content = new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8);

            // 提取基本信息
            String id = extractXmlValue(content, "id");
            String name = extractXmlValue(content, "name");
            String version = extractXmlValue(content, "version");
            String author = extractXmlValue(content, "author");
            String description = extractXmlValue(content, "description");
            int priority = Integer.parseInt(extractXmlValue(content, "priority", "100"));
            int startPriority = Integer.parseInt(extractXmlValue(content, "start-priority", "1000"));
            String mainClass = extractXmlValue(content, "main-class");

            if (id == null || name == null || version == null) {
                log.error("Missing required fields in extension XML: {}", xmlFile);
                return null;
            }

            return ExtensionMetadata.builder().id(id).name(name).version(version).author(author)
                    .description(description).priority(priority).startPriority(startPriority).mainClass(mainClass)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse extension XML: {}", xmlFile, e);
            return null;
        }
    }

    /**
     * 从XML内容中提取值
     * 
     * @param content XML内容
     * @param tagName 标签名
     * @return 标签值
     */
    @Nullable
    private String extractXmlValue(@NotNull String content, @NotNull String tagName) {
        return extractXmlValue(content, tagName, null);
    }

    /**
     * 从XML内容中提取值
     * 
     * @param content      XML内容
     * @param tagName      标签名
     * @param defaultValue 默认值
     * @return 标签值
     */
    @Nullable
    private String extractXmlValue(@NotNull String content, @NotNull String tagName, @Nullable String defaultValue) {
        try {
            String startTag = "<" + tagName + ">";
            String endTag = "</" + tagName + ">";

            int startIndex = content.indexOf(startTag);
            if (startIndex == -1) {
                return defaultValue;
            }

            startIndex += startTag.length();
            int endIndex = content.indexOf(endTag, startIndex);
            if (endIndex == -1) {
                return defaultValue;
            }

            return content.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            log.debug("Failed to extract XML value for tag: {}", tagName, e);
            return defaultValue;
        }
    }
}
