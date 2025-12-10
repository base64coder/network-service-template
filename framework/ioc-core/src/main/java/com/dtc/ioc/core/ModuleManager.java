package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块管理器
 * 负责管理IoC模块的加载和配置
 * 
 * @author Network Service Template
 */
public class ModuleManager {
    
    private static final Logger log = LoggerFactory.getLogger(ModuleManager.class);
    
    private final List<NetModule> modules = new ArrayList<>();
    private final Map<String, NetModule> moduleMap = new HashMap<>();
    
    /**
     * 添加模块
     * @param module 网络模块
     */
    public void addModule(@NotNull NetModule module) {
        if (moduleMap.containsKey(module.getModuleName())) {
            log.warn("⚠️ 模块已存在: {}", module.getModuleName());
            return;
        }
        
        modules.add(module);
        moduleMap.put(module.getModuleName(), module);
        log.debug("📦 已添加模块: {} v{}", module.getModuleName(), module.getModuleVersion());
    }
    
    /**
     * 配置所有模块
     * @param context 应用上下文
     */
    public void configureModules(@NotNull NetApplicationContext context) {
        log.info("⚙️ 正在配置 {} 个模块...", modules.size());
        
        // 按依赖顺序排序模块
        List<NetModule> sortedModules = sortModulesByDependencies();
        
        for (NetModule module : sortedModules) {
            try {
                log.debug("⚙️ 正在配置模块: {}", module.getModuleName());
                module.configure(context);
                log.debug("✅ 模块配置成功: {}", module.getModuleName());
            } catch (Exception e) {
                log.error("❌ 模块配置失败: {}", module.getModuleName(), e);
                throw new RuntimeException("模块配置失败: " + module.getModuleName(), e);
            }
        }
        
        log.info("✅ 所有模块配置成功");
    }
    
    /**
     * 按依赖关系排序模块
     */
    @NotNull
    private List<NetModule> sortModulesByDependencies() {
        List<NetModule> sorted = new ArrayList<>();
        List<NetModule> remaining = new ArrayList<>(modules);
        
        while (!remaining.isEmpty()) {
            boolean progress = false;
            
            for (int i = remaining.size() - 1; i >= 0; i--) {
                NetModule module = remaining.get(i);
                if (allDependenciesResolved(module, sorted)) {
                    sorted.add(module);
                    remaining.remove(i);
                    progress = true;
                }
            }
            
            if (!progress) {
                // 检测循环依赖
                StringBuilder cycle = new StringBuilder();
                for (NetModule module : remaining) {
                    cycle.append(module.getModuleName()).append(" -> ");
                }
                throw new RuntimeException("检测到循环依赖: " + cycle.toString());
            }
        }
        
        return sorted;
    }
    
    /**
     * 检查模块的所有依赖是否已解析
     */
    private boolean allDependenciesResolved(NetModule module, List<NetModule> resolved) {
        String[] dependencies = module.getDependencies();
        for (String dependency : dependencies) {
            boolean found = false;
            for (NetModule resolvedModule : resolved) {
                if (resolvedModule.getModuleName().equals(dependency)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取模块信息
     * @return 模块信息列表
     */
    @NotNull
    public List<ModuleInfo> getModuleInfo() {
        List<ModuleInfo> infoList = new ArrayList<>();
        for (NetModule module : modules) {
            ModuleInfo info = new ModuleInfo(
                module.getModuleName(),
                module.getModuleVersion(),
                module.getModuleDescription(),
                module.getDependencies()
            );
            infoList.add(info);
        }
        return infoList;
    }
    
    /**
     * 获取模块数量
     * @return 模块数量
     */
    public int getModuleCount() {
        return modules.size();
    }
    
    /**
     * 检查模块是否存在
     * @param moduleName 模块名称
     * @return 是否存在
     */
    public boolean hasModule(String moduleName) {
        return moduleMap.containsKey(moduleName);
    }
    
    /**
     * 获取模块
     * @param moduleName 模块名称
     * @return 模块实例
     */
    public NetModule getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }
    
    /**
     * 模块信息类
     */
    public static class ModuleInfo {
        private final String name;
        private final String version;
        private final String description;
        private final String[] dependencies;
        
        public ModuleInfo(String name, String version, String description, String[] dependencies) {
            this.name = name;
            this.version = version;
            this.description = description;
            this.dependencies = dependencies;
        }
        
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
        public String[] getDependencies() { return dependencies; }
    }
}
