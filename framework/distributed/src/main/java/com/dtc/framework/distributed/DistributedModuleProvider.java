package com.dtc.framework.distributed;

import com.dtc.core.bootstrap.ioc.ModuleProvider;
import com.dtc.framework.beans.IoCModule; // Changed from Module
import java.util.Collection;
import java.util.Collections;

public class DistributedModuleProvider implements ModuleProvider {
    @Override
    public Collection<IoCModule> getModules() { // Changed return type
        return Collections.singletonList(new DistributedModule());
    }
}

