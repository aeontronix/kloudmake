/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.service.host.Host;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

public class JavaResourceFactoryTest extends AbstractContextTest {
    @Test
    public void testInjectContext() throws Throwable {
        registerAndCreate(InjectContext.class, "injectctx").execute();
    }

    public static class InjectContext {
        @Inject
        private STContext context;

        @Prepare
        public void test() {
            assertEquals(context, STContext.get());
        }
    }

    @Test
    public void testInjectResource() throws Throwable {
        try {
            register(InjectResource.class);
            Resource resource = create(InjectResource.class);
            execute();
            assertTrue(resource == InjectResource.res.get());
        } finally {
            InjectResource.res.remove();
        }
    }

    public static class InjectResource {
        private static ThreadLocal<Resource> res = new ThreadLocal<>();
        @Inject
        private Resource resource;

        @Prepare
        public void test() {
            res.set(resource);
        }
    }

    @Test
    public void testAction() throws Throwable {
        registerAndCreate(ActionSingleStages.class).execute();
        ActionSingleStages javaAction = findJavaAction(ActionSingleStages.class);
        assertTrue(javaAction.prepared);
        assertTrue(javaAction.executed);
        assertTrue(javaAction.cleaned);
    }

    public static class ActionSingleStages {
        private boolean prepared;
        private boolean executed;
        private boolean cleaned;

        @Prepare
        public void prepare() {
            prepared = true;
        }

        @Execute
        public void exec() {
            executed = true;
        }

        @Cleanup
        public void clean() {
            cleaned = true;
        }
    }

    @Test(dependsOnMethods = "testAction")
    public void testActionMultipleStages() throws Throwable {
        registerAndCreate(ActionMultipleStages.class).execute();
        ActionMultipleStages javaAction = findJavaAction(ActionMultipleStages.class);
        assertEquals(2, javaAction.count);
    }

    public static class ActionMultipleStages {
        private int count;

        @Prepare
        @Execute
        public void test() {
            count++;
        }
    }

    @Test(dependsOnMethods = "testAction")
    public void testInjectServiceByClass() throws Throwable {
        registerAndCreate(InjectServiceByClass.class).execute();
    }

    public static class InjectServiceByClass {
        @Service
        private FileStore fsservice;
        @Service
        private Host hostservice;

        @Execute
        public void test() {
            assertNotNull(hostservice);
            assertNotNull(fsservice);
            assertTrue(hostservice instanceof Host);
            assertTrue(fsservice instanceof FileStore);
        }
    }

    @Test(dependsOnMethods = "testAction")
    public void testInjectServiceByName() throws Throwable {
        registerAndCreate(InjectServiceByFieldName.class).execute();
    }

    public static class InjectServiceByFieldName {
        @Service
        private FileStore filestore;
        @Service
        private Host host;

        @Execute
        public void test() {
            assertNotNull(host);
            assertNotNull(filestore);
            assertTrue(host instanceof Host);
            assertTrue(filestore instanceof FileStore);
        }
    }

    @Test(dependsOnMethods = "testAction")
    public void testInjectServiceByAnnoName() throws Throwable {
        registerAndCreate(InjectServiceByAnnoName.class).execute();
    }

    public static class InjectServiceByAnnoName {
        @Service("filestore")
        private FileStore filestoreserv;
        @Service("host")
        private Host hostserv;

        @Execute
        public void test() {
            assertNotNull(hostserv);
            assertNotNull(filestoreserv);
            assertTrue(hostserv instanceof Host);
            assertTrue(filestoreserv instanceof FileStore);
        }
    }

    @Test(dependsOnMethods = "testAction")
    public void testInjectServiceWithInject() throws Throwable {
        registerAndCreate(InjectServiceWithInjectAnno.class).execute();
    }

    public static class InjectServiceWithInjectAnno {
        @Inject
        private FileStore filestoreserv;
        @Inject
        private Host hostserv;

        @Execute
        public void test() {
            assertNotNull(hostserv);
            assertNotNull(filestoreserv);
            assertTrue(hostserv instanceof Host);
            assertTrue(filestoreserv instanceof FileStore);
        }
    }

    @Test(dependsOnMethods = "testAction", expectedExceptions = FieldInjectionException.class)
    public void testInjectServiceByInvalidName() throws Throwable {
        registerAndCreate(InjectServiceByInvalidName.class).execute();
    }

    public static class InjectServiceByInvalidName {
        @Service
        private Object filestoreWhoKnows;

        @Execute
        public void test() {
        }
    }

    @Test
    public void testInjectChildResources() throws Throwable {
        register(InjectChildResources.class);
        Resource r1 = createTestResource();
        Resource r2 = create(InjectChildResources.class);
        Resource r3 = createTestResource();
        r3.setParent(r2);
        Resource r4 = createTestResource();
        r4.setParent(r2);
        createTestResource();
        InjectChildResources impl = findJavaAction(InjectChildResources.class);
        assertContainsSame(impl.childrensPersist,r3,r4);
    }

    public static class InjectChildResources {
        @Resources("childof")
        private List<Resource> childrens;
        private List<Resource> childrensPersist;

        @Execute
        public void test() {
            childrensPersist = childrens;
        }
    }
}
