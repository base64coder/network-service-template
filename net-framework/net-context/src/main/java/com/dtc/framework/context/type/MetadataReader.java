package com.dtc.framework.context.type;

import java.io.IOException;

public interface MetadataReader {
    ClassMetadata getClassMetadata();
    AnnotationMetadata getAnnotationMetadata();
}

