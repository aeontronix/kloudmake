/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * Tests for the resource query language.
 */
public class ResourceQueryTest extends AbstractContextTest {
    @Test
    public void testAttrCISEq() throws Throwable {
        createJavaTestResource();
        Resource rs2 = createJavaTestResource().set("attr", "val1");
        Resource rs3 = createJavaTestResource().set("attr", "Val1");
        createJavaTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr like 'val1'");
        assertContainsSame(result, rs2, rs3);
        List<Resource> result2 = resourceManager.findResources("@attr = 'val1'");
        assertContainsSame(result2, rs2, rs3);
    }

    @Test
    public void testAttrGt() throws Throwable {
        createJavaTestResource();
        createJavaTestResource().set("attr", "2");
        Resource rs2 = createJavaTestResource().set("attr", "22");
        Resource rs3 = createJavaTestResource().set("attr", "20");
        createJavaTestResource().set("attr", "12");
        List<Resource> result = resourceManager.findResources("@attr gt 15");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrGt2() throws Throwable {
        createJavaTestResource();
        createJavaTestResource().set("attr", "d");
        Resource rs2 = createJavaTestResource().set("attr", "x");
        Resource rs3 = createJavaTestResource().set("attr", "z");
        createJavaTestResource().set("attr", "b");
        List<Resource> result = resourceManager.findResources("@attr gt f");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrLt() throws Throwable {
        createJavaTestResource().set("attr", "100");
        Resource rs1 = createJavaTestResource();
        Resource rs2 = createJavaTestResource().set("attr", "22");
        createJavaTestResource().set("attr", "120");
        List<Resource> result = resourceManager.findResources("@attr lt 50");
        assertContainsSame(result, rs1, rs2);
    }

    @Test
    public void testAttrLt2() throws Throwable {
        createJavaTestResource().set("attr", "y");
        Resource rs2 = createJavaTestResource();
        Resource rs3 = createJavaTestResource().set("attr", "a");
        createJavaTestResource().set("attr", "z");
        List<Resource> result = resourceManager.findResources("@attr lt f");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrCSEq() throws Throwable {
        createJavaTestResource().set("attr", "val1");
        Resource rs2 = createJavaTestResource().set("attr", "Val1");
        Resource rs3 = createJavaTestResource().set("attr", "Val1");
        List<Resource> result = resourceManager.findResources("@attr eq 'Val1'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNull() throws Throwable {
        createJavaTestResource().set("attr", "val1");
        Resource rs2 = createJavaTestResource();
        Resource rs3 = createJavaTestResource();
        List<Resource> result = resourceManager.findResources("@attr is null");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNotNull() throws Throwable {
        createJavaTestResource();
        Resource rs2 = createJavaTestResource().set("attr", "val1");
        Resource rs3 = createJavaTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr is not null");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsEmpty() throws Throwable {
        createJavaTestResource().set("attr", "val1");
        Resource rs2 = createJavaTestResource().set("attr", "");
        Resource rs3 = createJavaTestResource().set("attr", "");
        List<Resource> result = resourceManager.findResources("@attr is empty");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNotEmpty() throws Throwable {
        createJavaTestResource().set("attr", "");
        Resource rs2 = createJavaTestResource().set("attr", "val1");
        Resource rs3 = createJavaTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr is not empty");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrRegex1() throws Throwable {
        createJavaTestResource().set("attr", "xxx");
        Resource rs2 = createJavaTestResource().set("attr", "vol");
        Resource rs3 = createJavaTestResource().set("attr", "val");
        Resource rs4 = createJavaTestResource().set("attr", "vala");
        List<Resource> result = resourceManager.findResources("@attr regex 'v.l'");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test
    public void testAttrRegex2() throws Throwable {
        createJavaTestResource().set("attr", "xxx");
        Resource rs2 = createJavaTestResource().set("attr", "vol");
        Resource rs3 = createJavaTestResource().set("attr", "val");
        createJavaTestResource().set("attr", "vala");
        createJavaTestResource().set("attr", "aval");
        createJavaTestResource().set("attr", "avala");
        List<Resource> result = resourceManager.findResources("@attr regex '^v.l$'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrNotLike() throws Throwable {
        createJavaTestResource().set("attr", "val1");
        createJavaTestResource().set("attr", "Val1");
        Resource rs2 = createJavaTestResource().set("attr", "val2");
        Resource rs3 = createJavaTestResource().set("attr", "Val2");
        List<Resource> result = resourceManager.findResources("@attr not like 'val1'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testOr() throws Throwable {
        createJavaTestResource("id1");
        Resource rs2 = createJavaTestResource("id2");
        Resource rs3 = createJavaTestResource("id3");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq 'id3'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testOr2() throws Throwable {
        createJavaTestResource("id1");
        Resource rs2 = createJavaTestResource("id2");
        Resource rs3 = createJavaTestResource("id3");
        Resource rs4 = createJavaTestResource("id4");
        createJavaTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq 'id3' or @id eq 'id4'");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test(dependsOnMethods = "testOr2")
    public void testStrings() throws Throwable {
        createJavaTestResource("id1");
        Resource rs2 = createJavaTestResource("id2");
        Resource rs3 = createJavaTestResource("id3");
        Resource rs4 = createJavaTestResource("id4");
        createJavaTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq id3 or @id eq \"id4\"");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test(dependsOnMethods = "testOr2")
    public void testBadString() throws Throwable {
        createJavaTestResource("id1");
        Resource rs2 = createJavaTestResource("id2");
        Resource rs3 = createJavaTestResource("id3");
        createJavaTestResource("id4");
        createJavaTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq id3 or @id eq \"'id4\"");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAnd() throws Throwable {
        createJavaTestResource().setUid("uid1");
        Resource rs2 = createJavaTestResource("id2").set("attr1", "val1").set("attr2", "val2");
        createJavaTestResource().set("uid", "uid3");
        List<Resource> result = resourceManager.findResources("@attr1 eq 'val1' and @attr2 eq 'val2'");
        assertContainsSame(result, rs2);
    }

    @Test
    public void testAnd2() throws Throwable {
        createJavaTestResource().setUid("uid1");
        Resource rs2 = createJavaTestResource("id2").set("attr1", "val1").set("attr2", "val2").set("attr3", "val3");
        createJavaTestResource().set("uid", "uid3");
        List<Resource> result = resourceManager.findResources("@attr1 eq 'val1' and @attr2 eq 'val2' and @attr3 eq 'val3'");
        assertContainsSame(result, rs2);
    }

    @Test
    public void testChildOfScope() throws Throwable {
        register(ChildOfScope.class, "childofscope");
        Resource parent = resourceManager.createResource("test:childofscope", null, null);
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        createChildTestResource(null, child2);
        createJavaTestResource();
        execute();
        ChildOfScope impl1 = parent.getJavaImpl(ChildOfScope.class);
        assertNotNull(impl1);
        ChildOfScope impl = impl1;
        assertContainsSame(impl.found, child1, child2);
    }

    public static class ChildOfScope {
        private List<Resource> found;

        @Execute
        public void query() throws InvalidQueryException {
            STContext ctx = STContext.get();
            found = ctx.findResources("childof");
        }
    }

    @Test
    public void testChildOfParam() throws Throwable {
        createJavaTestResource();
        Resource parent = createJavaTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        createChildTestResource(null, child2);
        createJavaTestResource();
        execute();
        List<Resource> childs = ctx.findResources("childof @id eq 'id'");
        assertContainsSame(childs, child1, child2);
    }

    @Test
    public void testChildOfRecursiveParam() throws Throwable {
        createJavaTestResource();
        Resource parent = createJavaTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        Resource child3 = createChildTestResource(null, child2);
        createJavaTestResource();
        execute();
        List<Resource> childs = ctx.findResources("childof* @id eq 'id'");
        assertContainsSame(childs, child1, child2, child3);
    }

    @Test(enabled = false)
    public void testDepOfScope() throws Throwable {
        register(DepOfScope.class, "depsofscope");
        Resource r1 = resourceManager.createResource("test:depsofscope", null, null);
        Resource r2 = createJavaTestResource();
        r2.addDependency(r1);
        Resource r3 = createJavaTestResource();
        r3.addDependency(r1);
        createJavaTestResource().addDependency(r3);
        createJavaTestResource();
        execute();
        DepOfScope impl1 = r1.getJavaImpl(DepOfScope.class);
        assertNotNull(impl1);
        DepOfScope impl = impl1;
        assertContainsSame(impl.found, r2, r3);
    }

    public static class DepOfScope {
        private List<Resource> found;

        @Execute
        public void query() throws InvalidQueryException {
            STContext ctx = STContext.get();
            found = ctx.findResources("depends");
        }
    }

    @Test(enabled = false)
    public void testDepOfParam() throws Throwable {
        createJavaTestResource();
        Resource res1 = createJavaTestResource("id");
        Resource res2 = createJavaTestResource();
        res2.addDependency(res1);
        Resource res3 = createJavaTestResource();
        res3.addDependency(res3);
        createJavaTestResource();
        execute();
        List<Resource> childs = ctx.findResources("depends @id eq 'id'");
        assertContainsSame(childs, res2, res3);
    }

    @Test(enabled = false)
    public void testDepOfRecursiveParam() throws Throwable {
        createJavaTestResource();
        Resource parent = createJavaTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        Resource child3 = createChildTestResource(null, child2);
        createJavaTestResource();
        execute();
        List<Resource> childs = ctx.findResources("depends* @id eq 'id'");
        assertContainsSame(childs, child1, child2, child3);
    }

    @Test
    public void testFindByType() throws Throwable {
        register(FindByType.class, "findbytype");
        createJavaTestResource();
        Resource r1 = resourceManager.createResource("test:findbytype");
        Resource r2 = resourceManager.createResource("test:findbytype");
        createJavaTestResource();
        execute();
        List<Resource> resources = ctx.findResources("type test:findbytype");
        assertContainsSame(resources, r1, r2);
    }

    public static class FindByType {
    }
}
