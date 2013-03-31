/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;


import com.kloudtek.systyrant.resource.*;
import com.kloudtek.systyrant.util.ReflectionHelper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;

public class NotificationTests extends AbstractContextTest {
    @Test
    public void testSortingOnNotificationsNoHandler() throws Throwable {
        Resource target = createTestResource("target");
        Resource afterDueNotAndDep = createTestResourceWithIndirectDepsSetup("afterDueNotAndDep");
        Resource beforeDueNot = createTestResourceWithIndirectDepsSetup("beforeDueNot");
        beforeDueNot.addNotification(target);
        afterDueNotAndDep.addNotification(target);
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
        beforeDueNot.addNotification(target);
        afterDueNotAndDep.addNotification(target);
        ReflectionHelper.set(target, "notificationRequireOrder", true);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        List<Resource> resources = (List<Resource>) ReflectionHelper.get(resourceManager, "resources");
        ResourceSorter.sort2(resources);
        assertEquals(resources.toArray(new Resource[resources.size()]), new Resource[]{beforeDueNot, target, afterDueNotAndDep});
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
        TestNotificationHandler notificationHandler = new TestNotificationHandler();
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
            resource.addNotification(res);
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

        @Override
        public void handleNotification(Notification notification) {
            notified++;
        }
    }
}
