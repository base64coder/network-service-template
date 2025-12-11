package com.dtc.framework.context.type.classreading;

import com.dtc.framework.context.type.AnnotationMetadata;
import com.dtc.framework.context.type.ClassMetadata;
import com.dtc.framework.context.type.MetadataReader;
import net.bytebuddy.jar.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;

public class SimpleMetadataReader implements MetadataReader {
    private final AnnotationMetadataReadingVisitor visitor;

    public SimpleMetadataReader(InputStream is) throws IOException {
        ClassReader classReader = new ClassReader(is);
        this.visitor = new AnnotationMetadataReadingVisitor();
        classReader.accept(visitor, ClassReader.SKIP_DEBUG);
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return visitor;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return visitor;
    }
}

