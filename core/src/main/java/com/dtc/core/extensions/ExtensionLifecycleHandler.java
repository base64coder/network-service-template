package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.network.custom.CustomCodecFactory;
import com.dtc.core.network.custom.CustomConnectionManager;
import com.dtc.core.network.custom.CustomServer;
import com.dtc.core.extensions.model.ExtensionEvent;
import com.dtc.core.extensions.model.ExtensionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.inject.Injector;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 扩展生命周期处理器
 * 处理扩展的生命周期事件
 * 
 * @author Network Service Template
 */
@Singleton
public class ExtensionLifecycleHandler {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLifecycleHandler.class);

    private final ExtensionManager extensionManager;
    private final ExtensionShutdownManager shutdownManager;
    private final ExtensionCreationManager creationManager;
    private final Injector injector;

    @Inject
    public ExtensionLifecycleHandler(@NotNull ExtensionManager extensionManager,
            @NotNull ExtensionShutdownManager shutdownManager,
            @NotNull ExtensionCreationManager creationManager,
            @NotNull Injector injector) {
        this.extensionManager = extensionManager;
        this.shutdownManager = shutdownManager;
        this.creationManager = creationManager;
        this.injector = injector;
    }

    public CompletableFuture<Void> handleExtensionEvents(@NotNull Collection<ExtensionEvent> events) {
        validateInput(events, "Extension events collection cannot be null");
        log.info("Handling {} extension events", events.size());

        return CompletableFuture.runAsync(() -> {
            for (ExtensionEvent event : events) {
                try {
                    validateInput(event, "Extension event cannot be null");
                    handleSingleExtensionEvent(event);
                } catch (Exception e) {
                    log.error("Failed to handle extension event: {}", event, e);
                    // 继续处理其他事件，不中断整个流程
                }
            }
        }).exceptionally(throwable -> {
            log.error("Critical error in extension event handling", throwable);
            throw new CompletionException("Extension event handling failed", throwable);
        });
    }

    /**
     * 验证输入参数
     */
    private void validateInput(Object input, String message) {
        if (input == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证扩展事件
     */
    private void validateExtensionEvent(@NotNull ExtensionEvent event) {
        validateInput(event, "Extension event cannot be null");
        validateInput(event.getExtensionId(), "Extension ID cannot be null or empty");
        if (event.getExtensionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Extension ID cannot be empty");
        }
    }

    private void handleSingleExtensionEvent(@NotNull ExtensionEvent event) {
        validateExtensionEvent(event);

        switch (event.getChange()) {
            case ENABLE:
                handleExtensionEnable(event);
                break;
            case DISABLE:
                handleExtensionDisable(event);
                break;
            case UPDATE:
                handleExtensionUpdate(event);
                break;
            case REMOVE:
                handleExtensionRemove(event);
                break;
            default:
                log.warn("Unknown extension event type: {}", event.getChange());
        }
    }

    /**
     * 处理扩展禁用事件
     */
    private void handleExtensionDisable(@NotNull ExtensionEvent event) {
        String extensionId = event.getExtensionId();
        try {
            log.info("Disabling extension: {}", extensionId);

            // 优雅停止扩展
            gracefulStopExtension(extensionId);

            log.info("Extension {} disabled successfully", extensionId);
        } catch (Exception e) {
            log.error("Failed to disable extension: {}", extensionId, e);
            throw new RuntimeException("Failed to disable extension", e);
        }
    }

    /**
     * 优雅停止扩展
     * 
     * @param extensionId 扩展ID
     */
    private void gracefulStopExtension(@NotNull String extensionId) throws Exception {
        NetworkExtension extension = extensionManager.getExtension(extensionId);
        if (extension == null) {
            log.warn("Extension {} not found for graceful stop", extensionId);
            return;
        }

        log.info("Starting graceful stop for extension: {}", extensionId);

        // 使用管理器提供的优雅停止方法
        shutdownManager.gracefulShutdownExtension(extension, 30000).join();

        log.info("Extension {} gracefully stopped", extensionId);
    }

    /**
     * 处理扩展更新事件
     */
    private void handleExtensionUpdate(@NotNull ExtensionEvent event) {
        String extensionId = event.getExtensionId();
        log.info("Updating extension: {}", extensionId);

        try {
            // 步骤1: 检查扩展是否存在
            NetworkExtension currentExtension = extensionManager.getExtension(extensionId);
            if (currentExtension == null) {
                log.warn("Extension {} not found for update, treating as new installation", extensionId);
                handleExtensionEnable(event);
                return;
            }

            // 步骤1.5: 准备更新前的检查
            if (!prepareForUpdate(extensionId)) {
                log.warn("Extension {} is not ready for update, skipping", extensionId);
                return;
            }

            // 步骤2: 优雅停止当前扩展
            log.info("Gracefully stopping current extension: {}", extensionId);
            shutdownManager.gracefulShutdownExtension(currentExtension, 30000).join();

            // 步骤3: 注销当前扩展
            log.info("Unregistering current extension: {}", extensionId);
            extensionManager.unregisterExtension(extensionId);

            // 步骤4: 等待资源释放，确保旧扩展完全停止
            Thread.sleep(100);

            // 步骤5: 加载新版本的扩展
            log.info("Loading new version of extension: {}", extensionId);
            ExtensionMetadata metadata = validateAndGetMetadata(event);
            String mainClass = metadata.getMainClass();

            if (mainClass == null || mainClass.trim().isEmpty()) {
                log.warn("Extension {} has no main class specified for update", extensionId);
                return;
            }

            ClassLoader extensionClassLoader = createExtensionClassLoader(extensionId);
            NetworkExtension newExtension = createCodeBuddyExtension(extensionClassLoader, mainClass, extensionId);

            // 验证新扩展实例是否创建成功
            if (newExtension == null) {
                String errorMsg = String.format("Failed to create new extension instance for update: %s (class: %s)",
                        extensionId, mainClass);
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            // 比较版本并验证
            String currentVersion = currentExtension.getVersion();
            String newVersion = newExtension.getVersion();
            log.info("Updating extension {} from version {} to version {}", extensionId, currentVersion, newVersion);

            // 检查新版本是否兼容
            if (!isVersionCompatible(currentVersion, newVersion)) {
                log.warn("Version compatibility check failed for extension {}: {} -> {}",
                        extensionId, currentVersion, newVersion);
                // 可以根据策略决定是否继续更新或回滚
            }

            // 步骤6: 注册新版本的扩展
            log.info("Registering new version of extension: {}", extensionId);
            extensionManager.registerExtension(newExtension, extensionId);

            // 步骤7: 启动新版本的扩展
            log.info("Starting new version of extension: {}", extensionId);
            extensionManager.startExtension(extensionId).join();

            log.info("Extension {} updated successfully from version {} to version {}",
                    extensionId, currentExtension.getVersion(), newExtension.getVersion());

            // 步骤8: 执行更新后的清理
            cleanupAfterUpdate(extensionId, true);

        } catch (Exception e) {
            log.error("Failed to update extension: {}", extensionId, e);

            // 执行失败后的清理
            cleanupAfterUpdate(extensionId, false);

            // 查找备份扩展，如果更新失败则恢复原扩展
            try {
                log.info("Attempting to restore original extension: {}", extensionId);
                handleExtensionEnable(event);
            } catch (Exception restoreException) {
                log.error("Failed to restore extension after update failure: {}", extensionId, restoreException);
            }

            throw new RuntimeException("Failed to update extension: " + extensionId, e);
        }
    }

    /**
     * 检查版本是否兼容
     * 
     * @param currentVersion 当前版本
     * @param newVersion     新版本
     * @return 是否兼容
     */
    private boolean isVersionCompatible(@NotNull String currentVersion, @NotNull String newVersion) {
        try {
            // 简单的版本比较逻辑
            // 可以通过路由管理器实现更复杂的版本比较
            if (currentVersion.equals(newVersion)) {
                log.debug("Versions are identical: {}", currentVersion);
                return true;
            }

            // 检查主版本号是否兼容，简单比较
            String[] currentParts = currentVersion.split("\\.");
            String[] newParts = newVersion.split("\\.");

            if (currentParts.length > 0 && newParts.length > 0) {
                int currentMajor = Integer.parseInt(currentParts[0]);
                int newMajor = Integer.parseInt(newParts[0]);

                // 主版本号相同或新版本主版本号大于等于当前版本
                return newMajor >= currentMajor;
            }

            return true; // 默认认为兼容
        } catch (Exception e) {
            log.warn("Failed to compare versions: {} vs {}, assuming compatible", currentVersion, newVersion, e);
            return true;
        }
    }

    /**
     * 准备扩展更新前的检查
     * 
     * @param extensionId 扩展ID
     * @return 是否准备就绪
     */
    private boolean prepareForUpdate(@NotNull String extensionId) {
        try {
            // 检查扩展是否存在且可以更新
            NetworkExtension extension = extensionManager.getExtension(extensionId);
            if (extension == null) {
                return false;
            }

            // 检查扩展状态
            if (!extension.isStarted()) {
                log.debug("Extension {} is not started, safe to update", extensionId);
                return true;
            }

            // 可以通过路由管理器进行其他检查，例如：
            // - 检查是否有未完成的请求
            // - 检查是否有未释放的资源
            // - 检查是否可以安全更新
            return true;
        } catch (Exception e) {
            log.warn("Failed to prepare extension {} for update: {}", extensionId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行扩展更新后的清理工作
     * 
     * @param extensionId 扩展ID
     * @param success     更新是否成功
     */
    private void cleanupAfterUpdate(@NotNull String extensionId, boolean success) {
        try {
            if (success) {
                log.info("Extension {} update completed successfully", extensionId);
                // 可以通过路由管理器执行成功后的清理工作
            } else {
                log.warn("Extension {} update failed, performing cleanup", extensionId);
                // 可以通过路由管理器执行失败后的清理工作
            }
        } catch (Exception e) {
            log.error("Failed to cleanup after extension {} update: {}", extensionId, e.getMessage());
        }
    }

    /**
     * 执行扩展移除后的清理工作
     * 
     * @param extensionId 扩展ID
     */
    private void cleanupAfterRemoval(@NotNull String extensionId) {
        try {
            log.info("Performing cleanup after extension removal: {}", extensionId);

            // 清理扩展相关的资源
            // 1. 清理类加载器
            cleanupExtensionClassLoader(extensionId);

            // 2. 清理扩展文件夹
            cleanupExtensionFolder(extensionId);

            // 3. 清理扩展缓存
            cleanupExtensionCache(extensionId);

            // 4. 清理扩展指标数据
            cleanupExtensionMetrics(extensionId);

            log.info("Extension {} cleanup completed", extensionId);
        } catch (Exception e) {
            log.error("Failed to cleanup after extension {} removal: {}", extensionId, e.getMessage());
        }
    }

    /**
     * 清理扩展类加载器
     * 
     * @param extensionId 扩展ID
     */
    private void cleanupExtensionClassLoader(@NotNull String extensionId) {
        try {
            log.debug("Cleaning up class loader for extension: {}", extensionId);
            // 可以通过路由管理器清理类加载器
            // 例如关闭类加载器、释放资源等
        } catch (Exception e) {
            log.warn("Failed to cleanup class loader for extension: {}", extensionId, e);
        }
    }

    /**
     * 清理扩展文件夹
     * 
     * @param extensionId 扩展ID
     */
    private void cleanupExtensionFolder(@NotNull String extensionId) {
        try {
            log.debug("Cleaning up folder for extension: {}", extensionId);
            // 可以通过路由管理器清理扩展文件夹
            // 例如删除临时文件、清理文件夹等
        } catch (Exception e) {
            log.warn("Failed to cleanup folder for extension: {}", extensionId, e);
        }
    }

    /**
     * 清理扩展缓存
     * 
     * @param extensionId 扩展ID
     */
    private void cleanupExtensionCache(@NotNull String extensionId) {
        try {
            log.debug("Cleaning up cache for extension: {}", extensionId);
            // 可以通过路由管理器清理缓存
            // 例如清理序列化缓存、清理其他缓存等
        } catch (Exception e) {
            log.warn("Failed to cleanup cache for extension: {}", extensionId, e);
        }
    }

    /**
     * 清理扩展指标数据
     * 
     * @param extensionId 扩展ID
     */
    private void cleanupExtensionMetrics(@NotNull String extensionId) {
        try {
            log.debug("Cleaning up metrics for extension: {}", extensionId);
            // 可以通过路由管理器清理指标数据
            // 例如清理统计信息、清理监控数据等
        } catch (Exception e) {
            log.warn("Failed to cleanup metrics for extension: {}", extensionId, e);
        }
    }

    /**
     * 处理扩展移除事件
     */
    private void handleExtensionRemove(@NotNull ExtensionEvent event) {
        String extensionId = event.getExtensionId();
        log.info("Removing extension: {}", extensionId);

        try {
            // 步骤1: 检查扩展是否存在
            NetworkExtension extension = extensionManager.getExtension(extensionId);
            if (extension == null) {
                log.warn("Extension {} not found for removal", extensionId);
                return;
            }

            // 步骤2: 优雅停止扩展
            log.info("Gracefully stopping extension before removal: {}", extensionId);
            shutdownManager.gracefulShutdownExtension(extension, 30000).join();

            // 步骤3: 注销扩展
            log.info("Unregistering extension: {}", extensionId);
            extensionManager.unregisterExtension(extensionId);

            // 步骤4: 执行移除后的清理
            cleanupAfterRemoval(extensionId);

            log.info("Extension {} removed successfully", extensionId);
        } catch (Exception e) {
            log.error("Failed to remove extension: {}", extensionId, e);

            // 尝试强制移除
            try {
                log.warn("Attempting to force remove extension: {}", extensionId);
                extensionManager.unregisterExtension(extensionId);
                log.info("Extension {} force removed", extensionId);
            } catch (Exception forceRemoveException) {
                log.error("Failed to force remove extension: {}", extensionId, forceRemoveException);
            }

            throw new RuntimeException("Failed to remove extension: " + extensionId, e);
        }
    }

    private void handleExtensionEnable(@NotNull ExtensionEvent event) {
        log.info("Enabling extension: {}", event.getExtensionId());
        try {
            ExtensionMetadata metadata = validateAndGetMetadata(event);
            String mainClass = metadata.getMainClass();

            if (mainClass == null || mainClass.trim().isEmpty()) {
                log.warn("Extension {} has no main class specified", event.getExtensionId());
                return;
            }

            ClassLoader extensionClassLoader = createExtensionClassLoader(event.getExtensionId());
            String extensionId = event.getExtensionId();
            NetworkExtension extension = createCodeBuddyExtension(extensionClassLoader, mainClass, extensionId);

            // 验证扩展实例是否创建成功
            if (extension == null) {
                String errorMsg = String.format("Failed to create extension instance for: %s (class: %s)",
                        extensionId, mainClass);
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            log.debug("Using extension ID: {} for extension: {}", extensionId, extension.getClass().getSimpleName());

            extensionManager.registerExtension(extension, extensionId);
            extensionManager.startExtension(extensionId).join();

            log.info("Extension {} enabled and started successfully", event.getExtensionId());

        } catch (Exception e) {
            log.error("Failed to enable extension: {}", event.getExtensionId(), e);
            throw new RuntimeException("Failed to enable extension: " + event.getExtensionId(), e);
        }
    }

    /**
     * 验证并获取扩展元数据
     */
    private ExtensionMetadata validateAndGetMetadata(@NotNull ExtensionEvent event) {
        ExtensionMetadata metadata = event.getMetadata();
        if (metadata == null) {
            throw new IllegalArgumentException(
                    "Extension metadata cannot be null for extension: " + event.getExtensionId());
        }
        return metadata;
    }

    @NotNull
    private ClassLoader createExtensionClassLoader(@NotNull String extensionId) {
        validateInput(extensionId, "Extension ID cannot be null");
        if (extensionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Extension ID cannot be empty");
        }

        try {
            Path extensionDir = findExtensionDirectory(extensionId);
            Path jarFile = findExtensionJarFile(extensionDir, extensionId);

            log.info("Loading extension JAR: {} for extension: {}", jarFile, extensionId);
            List<URL> urls = buildClasspathUrls(jarFile, extensionDir);

            return new URLClassLoader(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            log.error("Failed to create extension class loader for: {}", extensionId, e);
            throw new RuntimeException("Failed to create extension class loader for: " + extensionId, e);
        }
    }

    /**
     * 构建类路径URL列表
     */
    @NotNull
    private List<URL> buildClasspathUrls(@NotNull Path jarFile, @NotNull Path extensionDir) throws Exception {
        List<URL> urls = new ArrayList<>();
        urls.add(jarFile.toUri().toURL());
        addDependencyJars(extensionDir, urls);
        return urls;
    }

    @NotNull
    private Path findExtensionDirectory(@NotNull String extensionId) {
        String[] possiblePaths = {
                "extensions/" + extensionId,
                "target/extensions/" + extensionId,
                "lib/extensions/" + extensionId
        };

        for (String pathStr : possiblePaths) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path) && Files.isDirectory(path)) {
                log.debug("Found extension directory: {} for extension: {}", path, extensionId);
                return path;
            }
        }

        String errorMsg = String.format("Extension directory not found for: %s. Searched paths: %s",
                extensionId, String.join(", ", possiblePaths));
        log.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    @NotNull
    private Path findExtensionJarFile(@NotNull Path extensionDir, @NotNull String extensionId) {
        try (var stream = Files.walk(extensionDir, 3)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> path.getFileName().toString().contains(extensionId))
                    .findFirst()
                    .orElseThrow(() -> {
                        String errorMsg = String.format("Extension JAR not found for: %s in directory: %s",
                                extensionId, extensionDir);
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });
        } catch (Exception e) {
            String errorMsg = String.format("Failed to search for extension JAR in directory: %s for extension: %s",
                    extensionDir, extensionId);
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    private void addDependencyJars(@NotNull Path extensionDir, @NotNull List<URL> urls) {
        try (var stream = Files.walk(extensionDir, 1)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .forEach(p -> {
                        try {
                            URL url = p.toUri().toURL();
                            if (!urls.contains(url)) {
                                urls.add(url);
                                log.debug("Added dependency JAR: {}", p);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to add dependency JAR: {}", p, e);
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to add dependency jars from {}", extensionDir, e);
        }
    }

    /**
     * 创建扩展实例
     * 
     * @param classLoader 类加载器
     * @param mainClass   主类
     * @param extensionId 扩展ID
     * @return 扩展实例
     */
    @NotNull
    private NetworkExtension createCodeBuddyExtension(@NotNull ClassLoader classLoader, @NotNull String mainClass,
            @NotNull String extensionId) {
        validateInput(classLoader, "ClassLoader cannot be null");
        validateInput(mainClass, "Main class cannot be null");
        validateInput(extensionId, "Extension ID cannot be null");

        try {
            log.info("🔍 Creating extension for class: {} with ID: {}", mainClass, extensionId);
            log.info("🔍 ClassLoader: {}", classLoader.getClass().getName());
            log.info("🔍 ClassLoader URLs: {}", java.util.Arrays.toString(((URLClassLoader) classLoader).getURLs()));

            // 加载扩展类
            Class<?> extensionClass = Class.forName(mainClass, true, classLoader);
            log.info("🔍 Loaded class: {} from classLoader: {}", extensionClass.getName(),
                    extensionClass.getClassLoader());

            // 使用管理器提供的创建方法创建扩展实例
            NetworkExtension extension = (NetworkExtension) creationManager.createEnhancedExtension(
                    extensionClass, classLoader, extensionId, getDependencyArgs(extensionId));

            // 记录创建信息
            log.info("🔍 Created extension instance: {} of type: {}",
                    extension.getClass().getName(), extension.getClass().getSimpleName());
            log.info("🔍 Extension implements NetworkExtension: {}", extension instanceof NetworkExtension);
            log.info("🔍 Extension is HttpExtension: {}", extension.getClass().getName().contains("HttpExtension"));

            return extension;

        } catch (ClassNotFoundException e) {
            String errorMsg = String.format("Extension class not found: %s for extension: %s", mainClass, extensionId);
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Failed to create extension for class: %s, extension: %s",
                    mainClass, extensionId);
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 获取扩展的依赖参数
     */
    @NotNull
    private Object[] getDependencyArgs(@NotNull String extensionId) {
        try {
            // 根据扩展ID获取对应的依赖参数
            if ("http-extension".equals(extensionId)) {
                return getHttpExtensionDependencies();
            } else if ("mqtt-extension".equals(extensionId)) {
                return getMqttExtensionDependencies();
            } else if ("tcp-extension".equals(extensionId)) {
                return getTcpExtensionDependencies();
            } else if ("websocket-extension".equals(extensionId)) {
                return getWebSocketExtensionDependencies();
            } else if ("custom-extension".equals(extensionId)) {
                return getCustomExtensionDependencies();
            }
            // 默认返回空参数
            return new Object[0];
        } catch (Exception e) {
            log.warn("Failed to get dependencies for extension: {}, using empty args", extensionId, e);
            return new Object[0];
        }
    }

    /**
     * 获取 HTTP 扩展的依赖参数
     */
    @NotNull
    private Object[] getHttpExtensionDependencies() {
        try {
            log.info("🔍 Attempting to get HTTP extension dependencies from IoC container...");

            // 从 IoC 容器中获取依赖，通过 Injector
            Object[] dependencies = new Object[8];

            try {
                dependencies[0] = injector.getInstance(com.dtc.core.http.HttpServer.class);
                dependencies[1] = injector.getInstance(com.dtc.core.http.HttpRequestHandler.class);
                dependencies[2] = injector.getInstance(com.dtc.core.http.HttpResponseHandler.class);
                dependencies[3] = injector.getInstance(com.dtc.core.http.HttpRouteManager.class);
                dependencies[4] = injector.getInstance(com.dtc.core.http.HttpMiddlewareManager.class);
                dependencies[5] = injector.getInstance(com.dtc.core.messaging.NetworkMessageQueue.class);
                dependencies[6] = injector.getInstance(com.dtc.core.statistics.StatisticsCollector.class);
                dependencies[7] = injector; // 传递 Injector 给扩展以便扩展可以获取其他依赖

                log.info("✅ Successfully obtained {} HTTP extension dependencies from Guice container",
                        dependencies.length);
                return dependencies;

            } catch (Exception e) {
                log.warn("⚠️  Failed to get HTTP dependencies from Guice container: {}", e.getMessage());
                log.warn("⚠️  This will cause ByteBuddy enhancement to fail and fall back to simple wrapper");
                return new Object[0];
            }

        } catch (Exception e) {
            log.error("Failed to get HTTP extension dependencies", e);
            return new Object[0];
        }
    }

    /**
     * 获取 MQTT 扩展的依赖参数
     */
    @NotNull
    private Object[] getMqttExtensionDependencies() {
        try {
            log.info("🔍 Attempting to get MQTT extension dependencies from Guice container...");

            // 从 Guice 容器中获取 MQTT 扩展的依赖
            Object[] dependencies = new Object[5];

            try {
                dependencies[0] = injector.getInstance(com.dtc.core.mqtt.MqttServer.class);
                dependencies[1] = injector.getInstance(com.dtc.core.mqtt.MqttMessageHelper.class);
                dependencies[2] = injector.getInstance(com.dtc.core.mqtt.MqttConnectionManager.class);
                dependencies[3] = injector.getInstance(com.dtc.core.messaging.NetworkMessageQueue.class);
                dependencies[4] = injector.getInstance(com.dtc.core.statistics.StatisticsCollector.class);

                log.info("✅ Successfully obtained {} MQTT extension dependencies from Guice container",
                        dependencies.length);
                return dependencies;

            } catch (Exception e) {
                log.warn("⚠️  Failed to get MQTT dependencies from Guice container: {}", e.getMessage());
                log.warn("⚠️  This will cause ByteBuddy enhancement to fail and fall back to simple wrapper");
                return new Object[0];
            }

        } catch (Exception e) {
            log.error("Failed to get MQTT extension dependencies", e);
            return new Object[0];
        }
    }

    /**
     * 获取 TCP 扩展的依赖参数
     */
    @NotNull
    private Object[] getTcpExtensionDependencies() {
        try {
            log.info("🔍 Attempting to get TCP extension dependencies from Guice container...");

            // 从 Guice 容器中获取 TCP 扩展的依赖
            Object[] dependencies = new Object[6];

            try {
                dependencies[0] = injector.getInstance(com.dtc.core.tcp.TcpServer.class);
                dependencies[1] = injector.getInstance(com.dtc.core.tcp.TcpMessageHelper.class);
                dependencies[2] = injector.getInstance(com.dtc.core.tcp.TcpConnectionManager.class);
                dependencies[3] = injector.getInstance(com.dtc.core.tcp.TcpProtocolHandler.class);
                dependencies[4] = injector.getInstance(com.dtc.core.messaging.NetworkMessageQueue.class);
                dependencies[5] = injector.getInstance(com.dtc.core.statistics.StatisticsCollector.class);

                log.info("✅ Successfully obtained {} TCP extension dependencies from Guice container",
                        dependencies.length);
                return dependencies;

            } catch (Exception e) {
                log.warn("⚠️  Failed to get TCP dependencies from Guice container: {}", e.getMessage());
                log.warn("⚠️  This will cause ByteBuddy enhancement to fail and fall back to simple wrapper");
                return new Object[0];
            }

        } catch (Exception e) {
            log.error("Failed to get TCP extension dependencies", e);
            return new Object[0];
        }
    }

    /**
     * 获取 WebSocket 扩展的依赖参数
     */
    @NotNull
    private Object[] getWebSocketExtensionDependencies() {
        try {
            log.info("🔍 Attempting to get WebSocket extension dependencies from Guice container...");

            // 从 Guice 容器中获取 WebSocket 扩展的依赖
            Object[] dependencies = new Object[5];

            try {
                dependencies[0] = injector.getInstance(com.dtc.core.websocket.WebSocketServer.class);
                dependencies[1] = injector.getInstance(com.dtc.core.websocket.WebSocketMessageHelper.class);
                dependencies[2] = injector.getInstance(com.dtc.core.websocket.WebSocketConnectionManager.class);
                dependencies[3] = injector.getInstance(com.dtc.core.messaging.NetworkMessageQueue.class);
                dependencies[4] = injector.getInstance(com.dtc.core.statistics.StatisticsCollector.class);

                log.info("✅ Successfully obtained {} WebSocket extension dependencies from Guice container",
                        dependencies.length);
                return dependencies;

            } catch (Exception e) {
                log.warn("⚠️  Failed to get WebSocket dependencies from Guice container: {}", e.getMessage());
                log.warn("⚠️  This will cause ByteBuddy enhancement to fail and fall back to simple wrapper");
                return new Object[0];
            }

        } catch (Exception e) {
            log.error("Failed to get WebSocket extension dependencies", e);
            return new Object[0];
        }
    }

    /**
     * 获取 Custom 扩展的依赖参数
     */
    @NotNull
    private Object[] getCustomExtensionDependencies() {
        try {
            log.info("🔍 Attempting to get Custom extension dependencies from Guice container...");

            // 从 Guice 容器中获取 Custom 扩展的依赖
            Object[] dependencies = new Object[6];

            try {
                dependencies[0] = injector.getInstance(CustomServer.class);
                dependencies[1] = injector.getInstance(com.dtc.core.custom.CustomMessageHelper.class);
                dependencies[2] = injector.getInstance(CustomConnectionManager.class);
                dependencies[3] = injector.getInstance(CustomCodecFactory.class);
                dependencies[4] = injector.getInstance(com.dtc.core.messaging.NetworkMessageQueue.class);
                dependencies[5] = injector.getInstance(com.dtc.core.statistics.StatisticsCollector.class);

                log.info("✅ Successfully obtained {} Custom extension dependencies from Guice container",
                        dependencies.length);
                return dependencies;

            } catch (Exception e) {
                log.warn("⚠️  Failed to get Custom dependencies from Guice container: {}", e.getMessage());
                log.warn("⚠️  This will cause ByteBuddy enhancement to fail and fall back to simple wrapper");
                return new Object[0];
            }

        } catch (Exception e) {
            log.error("Failed to get Custom extension dependencies", e);
            return new Object[0];
        }
    }
}
