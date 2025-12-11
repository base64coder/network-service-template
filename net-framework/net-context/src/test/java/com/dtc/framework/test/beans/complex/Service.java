package com.dtc.framework.test.beans.complex;

public class Service {
    private final Repository repository;
    
    public Service(Repository repository) {
        this.repository = repository;
    }
    
    public Repository getRepository() {
        return repository;
    }
}

