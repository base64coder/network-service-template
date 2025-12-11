package com.dtc.framework.persistent.transaction.support;

import com.dtc.framework.persistent.transaction.TransactionDefinition;

public class DefaultTransactionDefinition implements TransactionDefinition {
    private int propagationBehavior = PROPAGATION_REQUIRED;
    private int isolationLevel = ISOLATION_DEFAULT;
    private boolean readOnly = false;
    private String name;

    @Override
    public int getPropagationBehavior() {
        return propagationBehavior;
    }

    public void setPropagationBehavior(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }

    @Override
    public int getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

