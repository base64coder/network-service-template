package com.dtc.framework.context.type;

import java.util.Set;

public interface AnnotationMetadata extends ClassMetadata {
    Set<String> getAnnotationTypes();
    boolean hasAnnotation(String annotationName);
    boolean hasMetaAnnotation(String metaAnnotationName);
}

