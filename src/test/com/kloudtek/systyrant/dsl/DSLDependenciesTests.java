package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.resource.Resource;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests resource dependencies declaration
 */
public class DSLDependenciesTests extends AbstractContextTest {
    @Test
    public void testDepRightLinked() throws Throwable {
        ctx.runDSLScript("import test; new test {id='r1'} -> new test {id='r2'}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        assertEquals(r1.getDependencies().size(),1);
        assertEquals(r2.getDependencies().size(),0);
        assertEquals(r1.getDependencies().iterator().next(),r2);
    }

    @Test
    public void testDepRightLinked2() throws Throwable {
        ctx.runDSLScript("import test; new test{id='r1'} -> new test {id='r2'} -> new test {id='r3'} -> new test {id='r4'}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        Resource r3 = ctx.findResourceByUid("r3");
        Resource r4 = ctx.findResourceByUid("r4");
        assertEquals(r1.getDependencies().size(),1);
        assertEquals(r2.getDependencies().size(),1);
        assertEquals(r3.getDependencies().size(),1);
        assertEquals(r4.getDependencies().size(),0);
        assertEquals(r1.getDependencies().iterator().next(),r2);
        assertEquals(r2.getDependencies().iterator().next(),r3);
        assertEquals(r3.getDependencies().iterator().next(),r4);
    }

    @Test
    public void testDepLeftLinked() throws Throwable {
        ctx.runDSLScript("import test; new test(id='r2') {} <- new test(id='r1') {}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        assertEquals(r1.getDependencies().size(),1);
        assertEquals(r2.getDependencies().size(),0);
        assertEquals(r1.getDependencies().iterator().next(),r2);
    }

    @Test
    public void testDepLinkedResources() throws Throwable {
        ctx.runDSLScript("import test; new test(id='r1') {} -> new test(id='r2') {} -> new test(id='r3') {} <- new test(id='r4') {} -> new test(id='r5') {}");
        execute();
        Resource r1 = ctx.findResourceByUid("r1");
        Resource r2 = ctx.findResourceByUid("r2");
        Resource r3 = ctx.findResourceByUid("r3");
        Resource r4 = ctx.findResourceByUid("r4");
        Resource r5 = ctx.findResourceByUid("r5");
        assertEquals(r1.getDependencies().size(),1);
        assertEquals(r2.getDependencies().size(),1);
        assertEquals(r3.getDependencies().size(),0);
        assertEquals(r4.getDependencies().size(),2);
        assertEquals(r5.getDependencies().size(),0);
        assertEquals(r1.getDependencies().iterator().next(),r2);
        assertEquals(r2.getDependencies().iterator().next(),r3);
        assertTrue(r4.getDependencies().contains(r3));
        assertTrue(r4.getDependencies().contains(r5));
    }
}
