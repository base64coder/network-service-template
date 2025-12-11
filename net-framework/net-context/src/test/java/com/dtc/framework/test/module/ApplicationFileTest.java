package com.dtc.framework.test.module;

import com.dtc.framework.context.AnnotationConfigApplicationContext;
import com.dtc.framework.beans.env.Environment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationFileTest {

    @Test
    public void testFileLoading() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        Environment env = context.getEnvironment();
        assertEquals("loaded", env.getProperty("app.file.value"));
        assertEquals("FileApp", env.getProperty("app.config.name"));
    }
}

