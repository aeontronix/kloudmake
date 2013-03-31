/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;


import com.kloudtek.systyrant.annotation.HandleNotification;
import com.kloudtek.systyrant.resource.*;
import com.kloudtek.systyrant.util.ReflectionHelper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class NotificationTests extends AbstractContextTest {
    @Test
    public void testSortingOnNotificationsNoHandler() throws Throwable {
        Resource target = createTestResource("target");
        Resource afterDueNotAndDep = createTestResourceWithIndirectDepsSetup("afterDueNotAndDep");
        Resource beforeDueNot = createTestResourceWithIndirectDepsSetup("beforeDueNot");
        beforeDueNot.addAutoNotification(target);
        afterDueNotAndDep.addAutoNotification(target);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        List<Resource> resources = (List<Resource>) ReflectionHelper.get(resourceManager, "resources");
        ResourceSorter.sort2(resources);
        assertEquals(resources.toArray(new Resource[resources.size()]), new Resource[]{target, afterDueNotAndDep, beforeDueNot});
    }

    @Test
    public void testSortingOnNotifications() throws Throwable {
        Resource target = createTestResource("target");
        Resource afterDueNotAndDep = createTestResourceWithIndirectDepsSetup("afterDueNotAndDep");
        Resource beforeDueNot = createTestResourceWithIndirectDepsSetup("beforeDueNot");
        beforeDueNot.addAutoNotification(target);
        afterDueNotAndDep.addAutoNotification(target);
        ReflectionHelper.set(target, "notificationRequireOrder", true);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        List<Resource> resources = (List<Resource>) ReflectionHelper.get(resourceManager, "resources");
        ResourceSorter.sort2(resources);
        assertEquals(resources.toArray(new Resource[resources.size()]), new Resource[]{beforeDueNot, target, afterDueNotAndDep});
    }


    @Test
    public void testHandleNotificationDefCat() throws Throwable {
        registerAndCreate(HandleNotificationDefCat.class, "notifdef");
        createTestResource().set("notify", "type test:notifdef");
        execute();
        assertTrue(findJavaAction(HandleNotificationDefCat.class).handled);
    }

    public static class HandleNotificationDefCat {
        private boolean handled;

        @HandleNotification()
        public void handle() {
            handled = true;
        }
    }

    @Test
    public void testNotificationReordering() throws Throwable {
        assert resourceManager.getResources().size() == 0;
        LinkedList<Resource> before = new LinkedList<>();
        LinkedList<Resource> after = new LinkedList<>();
        resourceManager.registerResourceDefinition(new ResourceDefinition("test", "test2"));
        for (int i = 0; i < 5; i++) {
            before.add(createTestResource("before-1-" + i));
        }
        Resource res = resourceManager.createResource("test:test2", "target");
        TestNotificationHandler notificationHandler = new TestNotificationHandler(true,false,false,null);
        res.addNotificationHandler(notificationHandler);
        for (int i = 0; i < 5; i++) {
            before.add(createTestResource("before-2-" + i));
        }
        for (int i = 0; i < 5; i++) {
            Resource r = createTestResource("after-" + i);
            r.addDependency(res);
            after.add(r);
        }
        for (Resource resource : before) {
            resource.addAutoNotification(res);
        }
        execute();
        List<Resource> resources = resourceManager.getResources();
        assertEquals(resources.size(), 16);
        for (int i = 0; i < 10; i++) {
            assertTrue(before.remove(resources.get(i)));
        }
        assertSame(res, resources.get(10));
        for (int i = 11; i < 16; i++) {
            assertTrue(after.remove(resources.get(i)));
        }
        assertEquals(notificationHandler.notified, 10);
    }

    static class TestNotificationHandler extends NotificationHandler {
        private int notified = 0;

        TestNotificationHandler() {
            super(true, true, true, null);
        }

        TestNotificationHandler(boolean reorder, boolean aggregate, boolean onlyIfAfter, String category) {
            super(reorder, aggregate, onlyIfAfter, category);
        }

        @Override
        public void handleNotification(Notification notification) {
            notified++;
        }
    }

    @Test
    public void testDontRunNotificationIfNotExec() throws Throwable {
        Resource target = createTestResource();
        TestNotificationHandler notificationHandler = new TestNotificationHandler(true, false, true, null);
        target.addNotificationHandler(notificationHandler);
        createTestResource().addAutoNotification(target);
        execute();
        assertSame(resourceManager.getResources().get(1),target);
        assertEquals(notificationHandler.notified, 0, "Notification was received when it shouldn't");
    }

    @Test
    public void testNotificationAggregation() throws Throwable {
        TestNotificationHandler notificationHandler = new TestNotificationHandler(false, true, false, null);
        Resource target = createTestResource();
        target.addNotificationHandler(notificationHandler);
        Resource res1a = createTestResource("1a");
        res1a.addAutoNotification(target);
        Resource res1b = createTestResource("1b");
        res1b.addAutoNotification(target);
        Resource res2a = createTestResource("2a",res1a);
        res2a.addAutoNotification(target);
        Resource res2b = createTestResource("2b",res1b);
        res2b.addAutoNotification(target);
        execute();
        assertEquals(notificationHandler.notified, 2, "Notification should have occured twice");
    }
}