/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Default;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.Param;
import com.kloudtek.systyrant.context.AbstractAction;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.LocalHost;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Those tests check {@link com.kloudtek.systyrant.dsl.DSLScript} objects produced by DSL code snippets.
 */
public class DSLParserTest extends AbstractContextTest {
    @Test
    public void testSameHost() throws Throwable {
        LocalHost h1 = new LocalHost();
        LocalHost h2 = new LocalHost();
        Resource r1 = createTestResource("match");
        r1.setHostOverride(h1);
        Resource r2 = createTestResource();
        r2.setHostOverride(h1);
        Resource r3 = createTestResource();
        r3.setHostOverride(h2);
        Resource r4 = createTestResource();
        r4.setHostOverride(h2);
        r2.addAction(new AbstractAction() {
            @Override
            public void execute(STContext context, Resource resource) throws STRuntimeException {
                try {
                    List<Resource> samehost = ctx.findResources("is samehost");
                    if (samehost.size() == 1) {
                        resource.set("found", samehost.iterator().next().getId());
                    }
                } catch (InvalidQueryException e) {
                    throw new STRuntimeException(e);
                }
            }
        });
        execute();
        assertEquals(r2.get("found"), "match");
    }

    @Test(expectedExceptions = ScriptException.class, expectedExceptionsMessageRegExp = ".*Unable to find resource.*")
    public void testCreateElementMissingImport() throws Throwable {
        ctx.runScript("test { 'testid': attr = 'val' }");
        assertFalse(ctx.execute());
    }

    @Test
    public void testDefineResourceWithDefaultAttr() throws Throwable {
        ctx.runScript("def atest(attr = 'testval') {} atest {}");
        execute();
        assertResources("default.atest:default.atest1");
        assertResourceAttrs("default.atest:default.atest1", "attr", "testval");
    }

    @Test
    public void testCreateElement() throws Throwable {
        ctx.runScript("import test; test { \"myid\" : attr = 'val', attr2=val2; }");
        execute();
        assertEquals(ctx.getResources().size(), 1);
        Resource resource = ctx.getResources().get(0);
        assertEquals(resource.getType().getName(), "test");
        assertEquals(resource.getType().getPkg(), "test");
        assertEquals(resource.get("attr"), "val");
        assertEquals(resource.get("attr2"), "val2");
        assertEquals(resource.getId(), "myid");
    }

    @Test
    public void testCreateElement2() throws Throwable {
        ctx.runScript("import test; test(id = 'myid');");
        execute();
        assertEquals(ctx.getResources().size(), 1);
        Resource resource = ctx.getResources().get(0);
        assertEquals(resource.getType().getName(), "test");
        assertEquals(resource.getType().getPkg(), "test");
        assertEquals(resource.get("id"), "myid");
    }

    @Test
    public void testCreateElementFqn() throws Throwable {
        ctx.runScript("test.test { \"myid\": attr = 'val' }");
        assertTrue(ctx.execute());
        assertEquals(ctx.getResources().size(), 1);
        Resource resource = ctx.getResources().get(0);
        assertEquals(resource.getType().getName(), "test");
        assertEquals(resource.getType().getPkg(), "test");
        assertEquals(resource.get("attr"), "val");
        assertEquals(resource.getId(), "myid");
    }

    @Test
    public void testCreateAutoLoadExplicitelyNamedElement() throws Throwable {
        ctx.runScript("com.kloudtek.systyrant.dsl.autoload { \"myid\": attr1 = 'val1' }");
        assertTrue(ctx.execute());
        assertResources("com.kloudtek.systyrant.dsl.autoload:myid", "test.test:myid.foo");
    }

    @Test
    public void testCreateAutoLoadImportedElement() throws Throwable {
        ctx.runScript("import foo.bar; import com.kloudtek.systyrant.dsl; autoload { \"myid\": attr1 = 'val1' }");
        assertTrue(ctx.execute());
        assertResources("com.kloudtek.systyrant.dsl.autoload:myid", "test.test:myid.foo");
        assertResourceParent("com.kloudtek.systyrant.dsl.autoload:myid", null);
    }

    @Test
    public void testCreateMultipleElements() throws Throwable {
        executeDSLResource("create-resources.stl");
        String[] res = {"default.test2el:myid1", "test3.test3el:myid2", "test.test:myid1.foo", "test.test:myid2.bar", "default.test2el:myid2.myc1d1", "test.test:myid2.myc1d1.foo"};
        assertResources(res);
        assertResourceAttrs(res[0], "attr1", "val1");
        assertResourceAttrs(res[1], "attr2", "val2", "attr3", "val2");
        assertResourceAttrs(res[2]);
        assertResourceAttrs(res[3]);
        assertResourceAttrs(res[4], "attr4", "val2");
        assertResourceAttrs(res[5]);
        assertResourceParent(res[2], res[0]);
        assertResourceParent(res[3], res[1]);
        assertResourceParent(res[0], null);
        assertResourceParent(res[5], res[4]);
        assertResourceParent(res[1], null);
    }

    @Test
    public void testInvokeMethod() throws Throwable {
        TestService service = registerService(TestService.class);
        executeDSL("dostuff('foo','bar',a4=ga,a5=true)");
        assertEquals(service.a1, "foo");
        assertEquals(service.a2, "bar");
        assertEquals(service.a3, "defvalue");
        assertEquals(service.a4, "ga");
        assertEquals(service.a5, true);
    }

    @Test
    public void testInvokeMethodInClass() throws Throwable {
        TestService service = registerService(TestService.class);
        ctx.runScript("def rtest { dostuff('foo','bar',a4=ga,a5=true) } rtest {}");
        execute();
        assertEquals(service.a1, "foo");
        assertEquals(service.a2, "bar");
        assertEquals(service.a3, "defvalue");
        assertEquals(service.a4, "ga");
        assertEquals(service.a5, true);
    }

    @Test()
    public void testVarSubFromParentsAttrs() throws Throwable {
        ctx.runScript("test.test(id='parent',attr='val') { test.test(id='child',a='foo',b=\"${a}\",c='$a',d=$a,e=$attr,f=\"${attr}bar\",g=\"bla\\${a}b\\\\o\") {} }");
        execute();
        Resource parent = ctx.findResourceByUid("parent");
        Resource child = ctx.findResourceByUid("parent.child");
        assertEquals(parent.get("attr"), "val");
        assertEquals(child.get("a"), "foo");
        assertEquals(child.get("b"), "foo");
        assertEquals(child.get("c"), "$a");
        assertEquals(child.get("d"), "foo");
        assertEquals(child.get("e"), "val");
        assertEquals(child.get("f"), "valbar");
        assertEquals(child.get("g"), "bla${a}b\\o");
    }

    @Test()
    public void testVarSubFromAssignedVarInDef() throws Throwable {
        ctx.runScript("def test.newtest() { $var='hello'; test.test( id='child', value = $var) {} } test.newtest {'parent':}");
        execute();
        Resource child = ctx.findResourceByUid("parent.child");
        assertEquals(child.get("value"), "hello");
    }

    @Test()
    public void testVarSubFromAssignedVarInPrepare() throws Throwable {
        ctx.runScript("test.test() { 'parent': $var='hello'; test.test( id='child', value = $var) {} }");
        execute();
        Resource child = ctx.findResourceByUid("parent.child");
        assertEquals(child.get("value"), "hello");
    }

    @Test
    public void createResourceDefinedByScript() throws Throwable {
        ctx.runScript("def mytest.mytest { test.test{} }");
        ctx.runScript("mytest.mytest {}");
        execute();
        assertEquals(data.resources.size(), 2);
    }

    public static class TestService {
        private String a1;
        private String a2;
        private String a3;
        private String a4;
        private boolean a5;

        @Method("dostuff")
        public synchronized void registerFile(@Param("a1") String a1, @Param("attr2") @Default("thatsthedef") String a2,
                                              @Default("defvalue") @Param("a3") String a3, @Param("a4") String a4, @Param("a5") boolean a5) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
        }
    }
}
