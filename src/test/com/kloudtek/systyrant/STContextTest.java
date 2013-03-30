/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.Unique;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.LocalHost;
import com.kloudtek.systyrant.resource.*;
import com.kloudtek.systyrant.util.ReflectionHelper;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.kloudtek.systyrant.resource.Resource.State.EXECUTED;
import static com.kloudtek.systyrant.resource.Resource.State.FAILED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        test1.addAction(new AbstractAction() {
            @Override
            public void execute(STContext context, Resource resource) throws STRuntimeException {
                try {
                    context.getResourceManager().createResource(TEST);
                } catch (ResourceCreationException e) {
                    throw new STRuntimeException(e.getMessage(),e);
                }
            }
        });
        execute();
    }

    @Test
    public void testResourcesDependenciesAndSorting() throws ResourceCreationException, STRuntimeException {
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
        assertBefore(test2, test1);
        assertBefore(test3, test2, test3_child1);
        assertBefore(test4, test2);
        assertBefore(test4, test2);
        assertContainsSame(test1.getIndirectDependencies(), test2, test3, test4);
        assertContainsSame(test2.getIndirectDependencies(), test3, test4);
        assertContainsSame(test3.getIndirectDependencies());
        assertContainsSame(test4.getIndirectDependencies(), test3);
        assertContainsSame(test3_child1.getIndirectDependencies(), test3);
    }

    @Test
    public void testDependenciesSetAsBeforeAttr() throws Throwable {
        Resource r1 = createTestResource();
        Resource r2 = createTestResource("someid");
        Resource r3 = createTestResource();
        r3.set("before", "someid");
        Resource r4 = createTestResource();
        createTestResource();
        execute();
        assertTrue(r1.getDependencies().isEmpty());
        assertEquals(r2.getDependencies().size(), 1);
        assertSame(r2.getDependencies().iterator().next(), r3);
        assertTrue(r3.getDependencies().isEmpty());
        assertTrue(r4.getDependencies().isEmpty());
    }

    @Test
    public void testDependenciesSetAsAfterAttr() throws Throwable {
        Resource r1 = createTestResource();
        Resource r2 = createTestResource("someid");
        Resource r3 = createTestResource();
        r3.set("after", "someid");
        Resource r4 = createTestResource();
        createTestResource();
        execute();
        assertTrue(r1.getDependencies().isEmpty());
        assertTrue(r2.getDependencies().isEmpty());
        assertEquals(r3.getDependencies().size(), 1);
        assertSame(r3.getDependencies().iterator().next(), r2);
        assertTrue(r4.getDependencies().isEmpty());
    }

    @Test(dependsOnMethods = "testCreateDuplicateGlobalUniqueElements")
    public void testCreateSingleUniqueElements() throws Throwable {
        ctx.getResourceManager().createResource(UNIQUETEST);
        execute();
    }

    @Test(expectedExceptions = MultipleUniqueResourcesFoundException.class)
    public void testCreateDuplicateGlobalUniqueElements() throws Throwable {
        ctx.getResourceManager().createResource(UNIQUETEST);
        ctx.getResourceManager().createResource(UNIQUETEST);
        execute();
    }

    @Test
    public void testCreateDuplicateHostUniqueElementsNoConflict() throws Throwable {
        register(HostUnique.class);
        Resource p1 = createTestResource();
        p1.setHostOverride(new LocalHost());
        createChild(HostUnique.class, p1);
        Resource p2 = createTestResource();
        p2.setHostOverride(new LocalHost());
        createChild(HostUnique.class, p2);
        execute();
    }

    @Test(expectedExceptions = MultipleUniqueResourcesFoundException.class)
    public void testCreateDuplicateHostUniqueElementsConflicts() throws Throwable {
        register(HostUnique.class);
        Resource p1 = createTestResource();
        createChild(HostUnique.class, p1);
        Resource p2 = createTestResource();
        createChild(HostUnique.class, p2);
        execute();
    }
    @Unique(UniqueScope.HOST)
    public static class HostUnique {
    }

    @Test
    public void testGenerateId() throws STRuntimeException, InvalidAttributeException, ResourceCreationException {
        Resource el1Parent = createTestResource("parent");
        Resource el1 = createChildJavaTestResource("test2", el1Parent);
        Resource el2 = createTestResource("testval");
        Resource el3 = createTestResource();
        Resource el4 = createTestResource();
        assertEquals(el1Parent.getUid(), "parent");
        assertEquals(el1.getUid(), "parent.test2");
        assertEquals(el2.getUid(), "testval");
        assertEquals(el3.getUid(), "test:test1");
        assertEquals(el4.getUid(), "test:test2");
    }

    @Test
    public void testFailurePropagation() throws InvalidAttributeException, STRuntimeException, ResourceCreationException {
        ctx.clearFatalException();
        Resource el1 = createTestResource("1");
        el1.addAction(new FailAction(Action.Type.EXECUTE));
        Resource el2 = createTestResource("2", el1);
        Resource el3 = createTestResource("3", el2);
        Resource el4 = createTestResource("4", el3);
        Resource el5 = createTestResource("5");
        el5.addAction(new FailAction(Action.Type.PREPARE));
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
        Resource rs1 = createJavaTestResource("1");
        Resource rs2 = createChildJavaTestResource("2", rs1);
        Resource rs3 = createChildJavaTestResource("3", rs2);
        Resource rs4 = createChildJavaTestResource("4", rs3);
        Resource rs5 = createJavaTestResource("5", rs4);
        execute();
        Integer rs1ts = rs1.getJavaImpl(TestResource.class).getPostChildrenOrder();
        Integer rs2ts = rs2.getJavaImpl(TestResource.class).getPostChildrenOrder();
        Integer rs3ts = rs3.getJavaImpl(TestResource.class).getPostChildrenOrder();
        Integer rs4ts = rs4.getJavaImpl(TestResource.class).getPostChildrenOrder();
        Integer rs5ts = rs5.getJavaImpl(TestResource.class).getPostChildrenOrder();
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

    @Test(expectedExceptions = ResourceCreationException.class, expectedExceptionsMessageRegExp = "There is already a resource with uid id1")
    public void testConflictingId() throws Throwable {
        createTestResource("id1");
        createTestResource("id1");
        execute();
    }

    @Test
    public void testVerifyNoChange() throws Throwable {
        Resource res = createTestResource("x");
        Action action = Mockito.mock(Action.class);
        when(action.getType()).thenReturn(Action.Type.EXECUTE);
        res.addAction(action);
        when(action.supports(ctx, res)).thenReturn(true);
        when(action.checkExecutionRequired(ctx, res)).thenReturn(false);
        execute();
        verify(action, Mockito.times(1)).checkExecutionRequired(ctx, res);
        verify(action, Mockito.never()).execute(ctx, res);
    }

    @Test
    public void testVerifyChanged() throws Throwable {
        Resource res = createTestResource("x");
        Action action = Mockito.mock(Action.class);
        when(action.getType()).thenReturn(Action.Type.EXECUTE);
        res.addAction(action);
        when(action.supports(ctx, res)).thenReturn(true);
        when(action.checkExecutionRequired(ctx, res)).thenReturn(true);
        execute();
        verify(action, Mockito.times(1)).checkExecutionRequired(ctx, res);
        verify(action, Mockito.times(1)).execute(ctx, res);
    }

    @Test
    public void resolveRequiresExisting() throws Throwable {
        resourceManager.registerResourceDefinition(new ResourceDefinition("test","test1"));
        resourceManager.registerResourceDefinition(new ResourceDefinition("test","test2"));
        resourceManager.registerResourceDefinition(new ResourceDefinition("xxx","test3"));
        Resource r1 = createTestResource();
        r1.set("requires","test:test1,test:test2( foo = 'bar' )");
        r1.addRequires("test3( foo = 'bar', ba = 'be' )");
        Resource r2 = resourceManager.createResource("test:test1");
        Resource r3 = resourceManager.createResource("test:test2");
        r3.set("foo","bar");
        resourceManager.createResource("test:test2");
        Resource r4 = resourceManager.createResource("xxx:test3");
        r4.set("foo","bar");
        r4.set("ba","be");
        resourceManager.createResource("xxx:test3");
        ctx.addImport("xxx");
        execute();
        assertContainsSame(r1.getDependencies(),r2,r3,r4);
    }

    @Test
    public void resolveRequiresCreates() throws Throwable {
        resourceManager.registerResourceDefinition(new ResourceDefinition("test","test1"));
        resourceManager.registerResourceDefinition(new ResourceDefinition("test","test2"));
        resourceManager.registerResourceDefinition(new ResourceDefinition("xxx", "test3"));
        Resource noMatchRes = resourceManager.createResource("xxx:test3");
        Resource r1 = createTestResource();
        r1.set("requires", "test:test1,test:test2( foo = 'bar' ),test3( foo = 'bar', ba = 'be' )");
        ctx.addImport("xxx");
        execute();
        assertEquals(resourceManager.getResources().size(),5);
        List<Resource> result1 = resourceManager.findResources("type test:test1");
        assertEquals(result1.size(),1);
        Resource r2 = result1.get(0);
        assertEquals(r2.getAttributes().size(),2);
        assertEquals(r2.getAttributes().get("id"),"test:test11");
        assertEquals(r2.getAttributes().get("uid"),"test:test11");
        List<Resource> result2 = resourceManager.findResources("type test:test2");
        assertEquals(result2.size(),1);
        Resource r3 = result2.get(0);
        assertEquals(r3.getAttributes().size(),3);
        assertEquals(r3.getAttributes().get("id"),"test:test21");
        assertEquals(r3.getAttributes().get("uid"),"test:test21");
        assertEquals(r3.getAttributes().get("foo"),"bar");
        List<Resource> result3 = resourceManager.findResources("type xxx:test3");
        assertEquals(result3.size(),2);
        Resource r4 = result3.get(result3.get(0) == noMatchRes ? 1 : 0);
        assertEquals(r4.getAttributes().size(),4);
        assertEquals(r4.getAttributes().get("id"),"xxx:test32");
        assertEquals(r4.getAttributes().get("uid"),"xxx:test32");
        assertEquals(r4.getAttributes().get("foo"),"bar");
        assertEquals(r4.getAttributes().get("ba"),"be");
        assertContainsSame(r1.getDependencies(),r2,r3,r4);
    }


    @Test
    public void testSortingOnNotificationsNoHandler() throws Throwable {
        Resource target = createTestResource("target");
        Resource afterDueNotAndDep = createTestResourceWithIndirectDepsSetup("afterDueNotAndDep");
        Resource beforeDueNot = createTestResourceWithIndirectDepsSetup("beforeDueNot");
        target.addSubscription(beforeDueNot);
        target.addSubscription(afterDueNotAndDep);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        List<Resource> resources = (List<Resource>) ReflectionHelper.get(resourceManager, "resources");
        ResourceSorter.sort2(resources);
        assertEquals(resources.toArray(new Resource[resources.size()]),new Resource[]{target,afterDueNotAndDep,beforeDueNot});
    }

    @Test
    public void testSortingOnNotifications() throws Throwable {
        Resource target = createTestResource("target");
        Resource afterDueNotAndDep = createTestResourceWithIndirectDepsSetup("afterDueNotAndDep");
        Resource beforeDueNot = createTestResourceWithIndirectDepsSetup("beforeDueNot");
        target.addSubscription(beforeDueNot);
        target.addSubscription(afterDueNotAndDep);
        ReflectionHelper.set(target, "notificationRequireOrder", true);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        List<Resource> resources = (List<Resource>) ReflectionHelper.get(resourceManager, "resources");
        ResourceSorter.sort2(resources);
        assertEquals(resources.toArray(new Resource[resources.size()]),new Resource[]{beforeDueNot,target,afterDueNotAndDep});
    }

    @Test
    public void testNotificationReordering() throws Throwable {
        assert resourceManager.getResources().size() == 0;
        LinkedList<Resource> before = new LinkedList<>();
        LinkedList<Resource> after = new LinkedList<>();
        resourceManager.registerResourceDefinition(new ResourceDefinition("test", "test2"));
        for (int i = 0; i < 50; i++) {
            before.add(createTestResource("before-1-"+i));
        }
        Resource res = resourceManager.createResource("test:test2","target");
        TestNotificationHandler notificationHandler =new TestNotificationHandler();
        res.addNotificationHandler(notificationHandler);
        for (int i = 0; i < 50; i++) {
            before.add(createTestResource("before-2-"+i));
        }
        for (int i = 0; i < 50; i++) {
            Resource r = createTestResource("after-"+i);
            r.addDependency(res);
            after.add(r);
        }
        res.addSubscriptions(before);
        execute();
        List<Resource> resources = resourceManager.getResources();
        assertEquals(resources.size(),151);
        for (int i = 0; i < 100; i++) {
            assertTrue(before.remove(resources.get(i)));
        }
        assertSame(res,resources.get(100));
        for (int i = 101; i < 149; i++) {
            assertTrue(after.remove(resources.get(i)));
        }
//        assertEquals(notificationHandler.notified,100);
    }

    static class TestNotificationHandler extends NotificationHandler{
        private int notified = 0;

        TestNotificationHandler() {
            super(true,true,true);
        }

        @Override
        public void handleNotification(Notification notification) {
            notified++;
        }
    }
}
