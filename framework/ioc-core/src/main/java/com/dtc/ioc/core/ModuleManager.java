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
    
    private final List<IoCModule> modules = new ArrayList<>();
    private final Map<String, IoCModule> moduleMap = new HashMap<>();
    
    /**
     * 添加模块
     * 
     * @param module IoC模块
     */
    public void addModule(@NotNull IoCModule module) {
        if (moduleMap.containsKey(module.getModuleName())) {
            log.warn("⚠️ Module already exists: {}", module.getModuleName());
            return;
        }
        
        modules.add(module);
        moduleMap.put(module.getModuleName(), module);
        log.debug("📝 Added module: {} v{}", module.getModuleName(), module.getModuleVersion());
    }
    
    /**
     * 配置所有模块
     * 
     * @param context 应用上下文
     */
    public void configureModules(@NotNull NetworkApplicationContext context) {
        log.info("🔧 Configuring {} modules...", modules.size());
        
        // 按依赖顺序排序模块
        List<IoCModule> sortedModules = sortModulesByDependencies();
        
        for (IoCModule module : sortedModules) {
            try {
                log.debug("🔧 Configuring module: {}", module.getModuleName());
                module.configure(context);
                log.debug("✅ Module configured successfully: {}", module.getModuleName());
            } catch (Exception e) {
                log.error("❌ Failed to configure module: {}", module.getModuleName(), e);
                throw new RuntimeException("Failed to configure module: " + module.getModuleName(), e);
            }
        }
        
        log.info("✅ All modules configured successfully");
    }
    
    /**
     * 按依赖关系排序模块
     */
    @NotNull
    private List<IoCModule> sortModulesByDependencies() {
        List<IoCModule> sorted = new ArrayList<>();
        List<IoCModule> remaining = new ArrayList<>(modules);
        
        while (!remaining.isEmpty()) {
            boolean progress = false;
            
            for (int i = remaining.size() - 1; i >= 0; i--) {
                IoCModule module = remaining.get(i);
                if (allDependenciesResolved(module, sorted)) {
                    sorted.add(module);
                    remaining.remove(i);
                    progress = true;
                }
            }
            
            if (!progress) {
                // 检测循环依赖
                StringBuilder cycle = new StringBuilder();
                for (IoCModule module : remaining) {
                    cycle.append(module.getModuleName()).append(" -> ");
                }
                throw new RuntimeException("Circular dependency detected: " + cycle.toString());
            }
        }
        
        return sorted;
    }
    
    /**
     * 检查模块的所有依赖是否已解析
     */
    private boolean allDependenciesResolved(IoCModule module, List<IoCModule> resolved) {
        String[] dependencies = module.getDependencies();
        for (String dependency : dependencies) {
            boolean found = false;
            for (IoCModule resolvedModule : resolved) {
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
     * 
     * @return 模块信息列表
     */
    @NotNull
    public List<ModuleInfo> getModuleInfo() {
        List<ModuleInfo> infoList = new ArrayList<>();
        for (IoCModule module : modules) {
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
     * 
     * @return 模块数量
     */
    public int getModuleCount() {
        return modules.size();
    }
    
    /**
     * 检查模块是否存在
     * 
     * @param moduleName 模块名称
     * @return 是否存在
     */
    public boolean hasModule(String moduleName) {
        return moduleMap.containsKey(moduleName);
    }
    
    /**
     * 获取模块
     * 
     * @param moduleName 模块名称
     * @return 模块实例
     */
    public IoCModule getModule(String moduleName) {
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
