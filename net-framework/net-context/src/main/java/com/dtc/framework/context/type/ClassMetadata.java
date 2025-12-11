package com.dtc.framework.context.type;

public interface ClassMetadata {
    String getClassName();
    boolean isInterface();
    boolean isAbstract();
    boolean isConcrete();
    boolean isFinal();
    boolean hasSuperClass();
    String getSuperClassName();
    String[] getInterfaceNames();
}

