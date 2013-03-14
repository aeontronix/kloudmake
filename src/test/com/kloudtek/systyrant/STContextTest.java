/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kloudtek.systyrant.resource.Resource.State.EXECUTED;
import static com.kloudtek.systyrant.resource.Resource.State.FAILED;
import static org.testng.Assert.*;

public class STContextTest extends AbstractContextTest {
    @Test
    public void testSimpleElementCreation() throws Throwable {
        createTestResource("test1");
        execute();
    }


    @Test(dependsOnMethods = "testSimpleElementCreation", expectedExceptions = ResourceCreationException.class)
    public void testCreateElementDuringExecutionStage() throws Throwable {
        Resource test1 = createTestResource("test1");
        test1.set("createElementDuringExecute", true);
        execute();
    }

    @Test
    public void testLifecycle() throws ResourceCreationException, STRuntimeException {
        Resource test1 = createTestResource("test1");
        Resource test2 = createTestResource("test2");
        Resource test3 = createTestResource("test3");
        Resource test4 = createTestResource("test4");
        test1.addDependency(test2);
        test2.addDependency(test3);
        test2.addDependency(test4);
        String child1 = "test3_child1";
        TestResource.createChild(test3, child1);
        test4.addDependency(test3);
        assertTrue(ctx.execute());
        List<Resource> els = resourceManager.findResourcesById(child1);
        assertEquals(els.size(), 1);
        Resource test3_child1 = els.get(0);
        assertTrue(test3_child1.getDependencies().contains(test3));
        assertBefore(test2,test1);
        assertBefore(test3,test2,test3_child1);
        assertBefore(test4,test2);
        assertBefore(test4,test2);
    }


    @Test()
    public void testCreateSingleUniqueElements() throws STRuntimeException, ResourceCreationException {
        ctx.getResourceManager().createResource(UNIQUETEST);
    }

    @Test(dependsOnMethods = "testCreateSingleUniqueElements", expectedExceptions = ResourceCreationException.class, expectedExceptionsMessageRegExp = "Cannot create more than one instance of test:uniquetest")
    public void testCreateDuplicateUniqueElements() throws STRuntimeException, ResourceCreationException {
        ctx.getResourceManager().createResource(UNIQUETEST);
        ctx.getResourceManager().createResource(UNIQUETEST);
    }

    @Test
    public void testGenerateId() throws STRuntimeException, InvalidAttributeException, ResourceCreationException {
        Resource el1 = createTestElement("uid", "test2");
        Resource el2 = createTestElement("id", "testval");
        Resource el3 = createTestResource();
        Resource el4 = createTestResource();
        ctx.validateAndGenerateIdentifiers();
        assertEquals(el1.getUid(), "test2");
        assertEquals(el2.getUid(), "testval");
        assertEquals(el3.getUid(), "test:test1");
        assertEquals(el4.getUid(), "test:test2");
    }

    @Test
    public void testFailurePropagation() throws InvalidAttributeException, STRuntimeException, ResourceCreationException {
        ctx.clearFatalException();
        Resource el1 = createTestResource("1");
        el1.set("failExecution", true);
        Resource el2 = createTestResource("2", el1);
        Resource el3 = createTestResource("3", el2);
        Resource el4 = createTestResource("4", el3);
        Resource el5 = createTestResource("5");
        el5.set("failPreparation", true);
        Resource el6 = createTestResource("6", el5);
        Resource el7 = createTestResource("7", el6);
        Resource el8 = createTestResource("8");
        Resource el9 = createTestResource("9");
        assertFalse(ctx.execute());
        assertEquals(el1.getState(), FAILED);
        assertEquals(el2.getState(), FAILED);
        assertEquals(el3.getState(), FAILED);
        assertEquals(el4.getState(), FAILED);
        assertEquals(el5.getState(), FAILED);
        assertEquals(el6.getState(), FAILED);
        assertEquals(el7.getState(), FAILED);
        assertSame(el8.getState(), EXECUTED);
        assertSame(el9.getState(), EXECUTED);
    }

    @Test
    public void testPostChildrenExecution() throws Throwable {
        Resource rs1 = createTestResource("1");
        Resource rs2 = createChildTestResource("2", rs1);
        Resource rs3 = createChildTestResource("3", rs2);
        Resource rs4 = createChildTestResource("4", rs3);
        Resource rs5 = createTestResource("5", rs4);
        execute();
        Integer rs1ts = TestResource.get(rs1).getPostChildrenOrder();
        Integer rs2ts = TestResource.get(rs2).getPostChildrenOrder();
        Integer rs3ts = TestResource.get(rs3).getPostChildrenOrder();
        Integer rs4ts = TestResource.get(rs4).getPostChildrenOrder();
        Integer rs5ts = TestResource.get(rs5).getPostChildrenOrder();
        assertNotNull(rs1ts);
        assertNotNull(rs2ts);
        assertNotNull(rs3ts);
        assertNull(rs4ts);
        assertNull(rs5ts);
        assertTrue(rs3ts.intValue() > 0);
        assertEquals(rs2ts.intValue(), rs3ts + 1);
        assertEquals(rs1ts.intValue(), rs2ts + 1);
    }

    @Test(expectedExceptions = InvalidAttributeException.class)
    public void testConflictingUid() throws Throwable {
        Resource rs1 = createTestResource();
        rs1.setUid("myuid");
        Resource rs2 = createTestResource();
        rs2.setUid("myuid");
        execute();
    }

    @Test(expectedExceptions = InvalidAttributeException.class)
    public void testConflictingId() throws Throwable {
        createTestResource("id1");
        createTestResource("id1");
        execute();
    }

    @Test
    public void testVerifyGobalNoChange() throws Throwable {
        Resource res = createTestResource("x");
        TestResource testResource = TestResource.get(res);
        testResource.setVerifyGlobal(true);
        execute();
        assertNull(testResource.getSyncGlobalTS());
        assertNotNull(testResource.getSyncSpecificTS());
    }

    @Test
    public void testVerifyGlobalChange() throws Throwable {
        Resource res = createTestResource("x");
        TestResource testResource = TestResource.get(res);
        execute();
        assertNotNull(testResource.getSyncGlobalTS());
        assertNotNull(testResource.getSyncSpecificTS());
    }

    @Test
    public void testVerifyGlobalAndSpecifyChange() throws Throwable {
        Resource res = createTestResource("x");
        TestResource testResource = TestResource.get(res);
        testResource.setVerifySpecific(true);
        testResource.setVerifyGlobal(true);
        execute();
        assertNull(testResource.getSyncGlobalTS());
        assertNull(testResource.getSyncSpecificTS());
    }
}
