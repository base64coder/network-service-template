package com.dtc.framework.aot.processor;

import com.dtc.framework.beans.annotation.Component;
import com.dtc.framework.beans.annotation.Configuration;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "com.dtc.framework.beans.annotation.Component",
        "com.dtc.framework.beans.annotation.Configuration"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ComponentProcessor extends AbstractProcessor {

    private final Set<String> componentClasses = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateIndexFile();
            return false;
        }

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;
                    componentClasses.add(typeElement.getQualifiedName().toString());
                }
            }
        }
        return false;
    }

    private void generateIndexFile() {
        if (componentClasses.isEmpty()) {
            return;
        }

        try {
            FileObject resource = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, "", "META-INF/dtc-components.index");
            
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(resource.openOutputStream()))) {
                for (String className : componentClasses) {
                    writer.write(className);
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, "Failed to generate component index: " + e.getMessage());
        }
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

