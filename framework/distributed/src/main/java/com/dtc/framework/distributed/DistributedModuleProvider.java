package com.dtc.framework.distributed;

import com.dtc.core.bootstrap.ioc.ModuleProvider;
import com.google.inject.Module;
import java.util.Collection;
import java.util.Collections;

public class DistributedModuleProvider implements ModuleProvider {
    @Override
    public Collection<Module> getModules() {
        return Collections.singletonList(new DistributedModule());
    }
}

