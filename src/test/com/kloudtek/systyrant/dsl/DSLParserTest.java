/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Default;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.Param;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.service.ServiceManagerImpl;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Those tests check {@link com.kloudtek.systyrant.dsl.DSLScript} objects produced by DSL code snippets.
 */
public class DSLParserTest extends AbstractContextTest {
    @Test(expectedExceptions = ScriptException.class, expectedExceptionsMessageRegExp = ".*Unable to find resource.*")
    public void testCreateElementMissingImport() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("new test { 'testid'=> attr = 'val' }");
        assertFalse(ctx.execute());
    }

    @Test
    public void testDefineResourceWithDefaultAttr() throws IOException, ScriptException, STRuntimeException {
        ctx.runDSLScript("def atest(attr = 'testval') {} new atest {}");
        execute();
        validateResources(ctx, "default:atest:uid:default:atest1");
        validateResourcesAttrs(ctx, "default:atest:uid:default:atest1", "attr", "testval");
    }

    @Test
    public void testCreateElement() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("import test; new test { \"myid\"=> attr = 'val', attr2=val2 }");
        assertTrue(ctx.execute());
        assertEquals(ctx.getResources().size(), 1);
        Resource resource = ctx.getResources().get(0);
        assertEquals(resource.getFQName().getName(), "test");
        assertEquals(resource.getFQName().getPkg(), "test");
        assertEquals(resource.get("attr"), "val");
        assertEquals(resource.get("attr2"), "val2");
        assertEquals(resource.getId(), "myid");
    }

    @Test
    public void testCreateElementFqn() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("new test:test { \"myid\"=> attr = 'val' }");
        assertTrue(ctx.execute());
        assertEquals(ctx.getResources().size(), 1);
        Resource resource = ctx.getResources().get(0);
        assertEquals(resource.getFQName().getName(), "test");
        assertEquals(resource.getFQName().getPkg(), "test");
        assertEquals(resource.get("attr"), "val");
        assertEquals(resource.getId(), "myid");
    }

    @Test
    public void testCreateAutoLoadExplicitelyNamedElement() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("new com.kloudtek.systyrant.dsl:autoload { \"myid\"=> attr1 = 'val1' }");
        assertTrue(ctx.execute());
        validateResources(ctx, "com.kloudtek.systyrant.dsl:autoload:uid:myid", "test:test:uid:myid.foo");
    }

    @Test
    public void testCreateAutoLoadImportedElement() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("import foo.bar; import com.kloudtek.systyrant.dsl; new autoload { \"myid\"=> attr1 = 'val1' }");
        assertTrue(ctx.execute());
        validateResources(ctx, "com.kloudtek.systyrant.dsl:autoload:uid:myid", "test:test:uid:myid.foo");
        validateParent("com.kloudtek.systyrant.dsl:autoload:uid:myid", null);
    }

    @Test
    public void testCreateMultipleElements() throws InvalidScriptException, InvalidResourceDefinitionException, URISyntaxException, STRuntimeException, IOException, ScriptException {
        ctx.runScript(getClass().getResource("create-resources.stl").toURI());
        assertTrue(ctx.execute());
        String[] res = {"default:test2el:uid:myid1", "test3:test3el:uid:myid2", "test:test:uid:myid1.foo", "test:test:uid:myid2.bar", "default:test2el:uid:myid2.myc1d1", "test:test:uid:myid2.myc1d1.foo"};
        validateResources(ctx, res);
        validateResourcesAttrs(ctx, res[0], "attr1", "val1");
        validateResourcesAttrs(ctx, res[1], "attr2", "val2", "attr3", "val2");
        validateResourcesAttrs(ctx, res[2]);
        validateResourcesAttrs(ctx, res[3]);
        validateResourcesAttrs(ctx, res[4], "attr4", "val2");
        validateResourcesAttrs(ctx, res[5]);
        validateParent(res[2], res[0]);
        validateParent(res[3], res[1]);
        validateParent(res[0], null);
        validateParent(res[5], res[4]);
        validateParent(res[1], null);
    }

    @Test
    public void testInvokeMethod() throws InvalidServiceException, IOException, ScriptException, STRuntimeException {
        TestService service = new TestService();
        ((ServiceManagerImpl) ctx.getServiceManager()).registerService("test", service);
        ctx.runDSLScript("dostuff('foo','bar',a4=ga,a5=true)");
        execute();
        assertEquals(service.a1, "foo");
        assertEquals(service.a2, "bar");
        assertNull(service.a3);
        assertEquals(service.a4, "ga");
        assertEquals(service.a5, true);
    }

    @Test
    public void testInvokeMethodInClass() throws InvalidServiceException, IOException, ScriptException, STRuntimeException {
        TestService service = new TestService();
        ((ServiceManagerImpl) ctx.getServiceManager()).registerService("test", service);
        ctx.runDSLScript("def test { dostuff('foo','bar',a4=ga,a5=true) } new test {}");
        execute();
        assertEquals(service.a1, "foo");
        assertEquals(service.a2, "bar");
        assertNull(service.a3);
        assertEquals(service.a4, "ga");
        assertEquals(service.a5, true);
    }

    @Test()
    public void testVariables() throws IOException, ScriptException, STRuntimeException {
        ctx.runDSLScript("new test:test(id='parent',attr='val') { new test:test(id='child',a='foo',b=\"${a}\",c='$a',d=$a,e=$attr,f=\"${attr}bar\",g=\"bla\\${a}b\\\\o\") {} }");
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

    private static void validateResourcesAttrs(STContext ctx, String uid, String... attrs) {
        Resource resource = findResource(ctx, uid);
        assertNotNull(resource, "Unable to find resource " + uid);
        HashMap<String, String> attr = new HashMap<>(resource.getAttributes());
        attr.remove("id");
        attr.remove("uid");
        for (int i = 0; i < attrs.length; i += 2) {
            String attrId = attrs[i];
            assertEquals(resource.get(attrId), attrs[i + 1]);
            attr.remove(attrId);
        }
        if (!attr.isEmpty()) {
            fail("Unexpected " + uid + " attributes: " + attr.toString());
        }
    }

    private void validateParent(String resource, String parent) {
        Resource actualParent = findResource(ctx, resource).getParent();
        Resource expectedParent = findResource(ctx, parent);
        if (parent != null) {
            assertEquals(actualParent, expectedParent);
        } else {
            assertNull(expectedParent);
        }
    }

    private static Resource findResource(STContext ctx, String uid) {
        for (Resource resource : ctx.getResourceManager()) {
            if (resource.toString().equals(uid)) {
                return resource;
            }
        }
        return null;
    }

    private static void validateResources(STContext ctx, String... elements) {
        assertEquals(ctx.getResources().size(), elements.length);
        List<String> found = new ArrayList<>();
        for (Resource el : ctx.getResources()) {
            found.add(el.toString());
        }
        for (String expected : elements) {
            assertTrue(found.remove(expected), "Did not find element " + expected);
        }
        if (!found.isEmpty()) {
            fail("Unexcepted resource " + found.iterator().next());
        }
    }

    public class TestService {
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
