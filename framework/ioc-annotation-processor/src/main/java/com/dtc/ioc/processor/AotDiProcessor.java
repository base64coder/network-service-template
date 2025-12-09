package com.dtc.ioc.processor;

import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * AOT Dependency Injection Processor.
 * Generates factory classes at compile time, similar to Dagger.
 */
@SupportedAnnotationTypes({"com.dtc.annotations.ioc.Component", "com.dtc.annotations.ioc.Service"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class AotDiProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Skeletal implementation for AOT generation
        // 1. Scan for @Component / @Service classes
        // 2. Generate Factory classes (e.g. MyService_Factory)
        // 3. Generate Module bindings if needed
        return false;
    }
}

