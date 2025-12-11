package com.dtc.framework.context.type.classreading;

import com.dtc.framework.context.type.AnnotationMetadata;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

import java.util.HashSet;
import java.util.Set;

public class AnnotationMetadataReadingVisitor extends ClassVisitor implements AnnotationMetadata {
    private String className;
    private boolean isInterface;
    private boolean isAbstract;
    private boolean isFinal;
    private String superClassName;
    private String[] interfaces;
    private final Set<String> annotationSet = new HashSet<>();

    public AnnotationMetadataReadingVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name.replace('/', '.');
        this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        this.isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        this.isFinal = (access & Opcodes.ACC_FINAL) != 0;
        if (superName != null) {
            this.superClassName = superName.replace('/', '.');
        }
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = interfaces[i].replace('/', '.');
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String className = Type.getType(descriptor).getClassName();
        this.annotationSet.add(className);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isConcrete() {
        return !(isInterface || isAbstract);
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean hasSuperClass() {
        return superClassName != null && !"java.lang.Object".equals(superClassName);
    }

    @Override
    public String getSuperClassName() {
        return superClassName;
    }

    @Override
    public String[] getInterfaceNames() {
        return interfaces;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationSet;
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return annotationSet.contains(annotationName);
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        return false;
    }
}

