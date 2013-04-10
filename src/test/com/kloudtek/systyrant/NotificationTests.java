/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;


import com.kloudtek.systyrant.annotation.HandleNotification;
import com.kloudtek.systyrant.context.*;
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
        beforeDueNot.addAutoNotification(target);
        afterDueNotAndDep.addAutoNotification(target);
        ReflectionHelper.set(afterDueNotAndDep, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(afterDueNotAndDep, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        STContextData data = (STContextData) ReflectionHelper.get(ctx, "data");
        ResourceSorter.bringResourcesForwardDueToNotification(data);
        assertEquals(data.resources.toArray(new Resource[data.resources.size()]), new Resource[]{target, afterDueNotAndDep, beforeDueNot});
    }

    @Test
    public void testSortingOnNotifications() throws Throwable {
        Resource target = createTestResource("target");
        target.addNotificationHandler(new TestNotificationHandler(true, false, false, null));

        Resource after = createTestResourceWithIndirectDepsSetup("after");
        ReflectionHelper.set(after, "dependencies", new HashSet<>(Arrays.asList(target)));
        ReflectionHelper.set(after, "indirectDependencies", new HashSet<>(Arrays.asList(target)));
        data.add(new AutoNotify(after, target, null));

        Resource before = createTestResourceWithIndirectDepsSetup("before");
        data.add(new AutoNotify(before, target, null));

        Resource cascadeTarget = createTestResourceWithIndirectDepsSetup("cascadeTarget");
        data.add(new AutoNotify(cascadeTarget, target, null));
        cascadeTarget.addNotificationHandler(new TestNotificationHandler(true, false, false, null));

        Resource cascadeSource = createTestResourceWithIndirectDepsSetup("cascadeSource");
        data.add(new AutoNotify(cascadeSource, cascadeTarget, null));

        ResourceSorter.bringResourcesForwardDueToNotification(data);
        assertEquals(data.resources.toArray(new Resource[data.resources.size()]), new Resource[]{before, cascadeSource, cascadeTarget, target, after});
    }

    @Test
    public void testHandleNotificationDefCat() throws Throwable {
        registerAndCreate(HandleNotificationDefCat.class, "notiftarget");
        createTestResource("notifier").set("notify", "type test:notiftarget");
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
    public void testNotifyOnlyAfterPositive() throws Throwable {
        registerAndCreate(NotifyOnlyAfter.class, "notiftarget");
        createTestResource("notifier").set("notify", "type test:notiftarget").set("before", "type test:notiftarget");
        execute();
        assertFalse(findJavaAction(NotifyOnlyAfter.class).handled);
    }

    @Test
    public void testNotifyOnlyAfterNegative() throws Throwable {
        registerAndCreate(NotifyOnlyAfter.class, "notiftarget");
        createTestResource("notifier").set("notify", "type test:notiftarget").set("after", "type test:notiftarget");
        execute();
        assertTrue(findJavaAction(NotifyOnlyAfter.class).handled);
    }

    public static class NotifyOnlyAfter {
        private boolean handled;

        @HandleNotification(onlyIfAfter = true)
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
        TestNotificationHandler notificationHandler = new TestNotificationHandler(true, false, false, null);
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
        assertSame(resourceManager.getResources().get(1), target);
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
        Resource res2a = createTestResource("2a", res1a);
        res2a.addAutoNotification(target);
        Resource res2b = createTestResource("2b", res1b);
        res2b.addAutoNotification(target);
        Resource res2b2 = createTestResource("2b2", res1b);
        res2b2.addAutoNotification(target);
        Resource res3b = createTestResource("3b", res2b);
        res3b.addAutoNotification(target);
        Resource res4b = createTestResource("4b", res3b);
        res4b.addAutoNotification(target);
        execute();
        assertEquals(notificationHandler.notified, 4);
    }
}
