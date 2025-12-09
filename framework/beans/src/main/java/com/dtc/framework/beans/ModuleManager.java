package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
     * æ¨¡åç®¡çå¨
è´è´£ç®¡çIoCæ¨¡åçå è½½åéç½®
@author Network Service Template
/
public class ModuleManager {
    
    private static final Logger log = LoggerFactory.getLogger(ModuleManager.class);
    
    private final List<IoCModule> modules = new ArrayList<>();
    private final Map<String, IoCModule> moduleMap = new HashMap<>();
    
    /**
     * æ·»å æ¨¡å
@param module IoCæ¨¡å
/
    public void addModule(@NotNull IoCModule module) {
        if (moduleMap.containsKey(module.getModuleName())) {
            log.warn("â ï¸ Module already exists: {}", module.getModuleName());
            return;
        }
        
        modules.add(module);
        moduleMap.put(module.getModuleName(), module);
        log.debug("ð Added module: {} v{}", module.getModuleName(), module.getModuleVersion());
    }
    
    /**
     * éç½®æææ¨¡å
@param context åºç¨ä¸ä¸æ
/
    public void configureModules(@NotNull NetworkApplicationContext context) {
        log.info("ð§ Configuring {} modules...", modules.size());
        
        // æä¾èµé¡ºåºæåºæ¨¡å
        List<IoCModule> sortedModules = sortModulesByDependencies();
        
        for (IoCModule module : sortedModules) {
            try {
                log.debug("ð§ Configuring module: {}", module.getModuleName());
                module.configure(context);
                log.debug("â Module configured successfully: {}", module.getModuleName());
            } catch (Exception e) {
                log.error("â Failed to configure module: {}", module.getModuleName(), e);
                throw new RuntimeException("Failed to configure module: " + module.getModuleName(), e);
            }
        }
        
        log.info("â All modules configured successfully");
    }
    
    /**
     * æä¾èµå³ç³»æåºæ¨¡å
/
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
                // æ£æµå¾ªç¯ä¾èµ
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
     * æ£æ¥æ¨¡åçææä¾èµæ¯å¦å·²è§£æ
/
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
     * è·åæ¨¡åä¿¡æ¯
@return æ¨¡åä¿¡æ¯åè¡¨
/
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
     * è·åæ¨¡åæ°é
@return æ¨¡åæ°é
/
    public int getModuleCount() {
        return modules.size();
    }
    
    /**
     * æ£æ¥æ¨¡åæ¯å¦å­å¨
@param moduleName æ¨¡ååç§°
@return æ¯å¦å­å¨
/
    public boolean hasModule(String moduleName) {
        return moduleMap.containsKey(moduleName);
    }
    
    /**
     * è·åæ¨¡å
@param moduleName æ¨¡ååç§°
@return æ¨¡åå®ä¾
/
    public IoCModule getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }
    
    /**
     * æ¨¡åä¿¡æ¯ç±»
/
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
