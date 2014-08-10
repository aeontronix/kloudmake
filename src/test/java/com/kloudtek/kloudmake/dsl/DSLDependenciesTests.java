/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.AbstractContextTest;
import com.kloudtek.kloudmake.Resource;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests resource dependencies declaration
 */
public class DSLDependenciesTests extends AbstractContextTest {
    @Test
    public void testDepRightLinked() throws Throwable {
        ctx.runScript("import test; test {id='r1'} -> test {id='r2'}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        assertEquals(r1.getDependencies().size(), 0);
        assertEquals(r2.getDependencies().size(), 1);
        assertEquals(r2.getDependencies().iterator().next(), r1);
    }

    @Test
    public void testDepRightLinked2() throws Throwable {
        ctx.runScript("import test; test{id='r1'} -> test {id='r2'} -> test {id='r3'} -> test {id='r4'}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        Resource r3 = ctx.findResourceByUid("r3");
        Resource r4 = ctx.findResourceByUid("r4");
        assertEquals(r1.getDependencies().size(), 0);
        assertEquals(r2.getDependencies().size(), 1);
        assertEquals(r3.getDependencies().size(), 1);
        assertEquals(r4.getDependencies().size(), 1);
        assertEquals(r2.getDependencies().iterator().next(), r1);
        assertEquals(r3.getDependencies().iterator().next(), r2);
        assertEquals(r4.getDependencies().iterator().next(), r3);
    }

    @Test
    public void testDepLeftLinked() throws Throwable {
        ctx.runScript("import test; test(id='r2') {} <- test(id='r1') {}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        assertEquals(r1.getDependencies().size(), 0);
        assertEquals(r2.getDependencies().size(), 1);
        assertEquals(r2.getDependencies().iterator().next(), r1);
    }

    @Test
    public void testDepLinkedResources() throws Throwable {
        ctx.runScript("import test; test(id='r1') {} -> test(id='r2') {} -> test(id='r3') {} <- test(id='r4') {} -> test(id='r5') {}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        Resource r3 = ctx.findResourceByUid("r3");
        Resource r4 = ctx.findResourceByUid("r4");
        Resource r5 = ctx.findResourceByUid("r5");
        assertEquals(r1.getDependencies().size(), 0);
        assertEquals(r2.getDependencies().size(), 1);
        assertEquals(r3.getDependencies().size(), 2);
        assertEquals(r4.getDependencies().size(), 0);
        assertEquals(r5.getDependencies().size(), 1);
        assertEquals(r2.getDependencies().iterator().next(), r1);
        assertEqualsNoOrder(r3.getDependencies().toArray(), new Object[]{r2, r4});
        assertTrue(r5.getDependencies().contains(r4));
    }
}
