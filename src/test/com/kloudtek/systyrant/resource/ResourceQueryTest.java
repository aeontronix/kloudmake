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

/**
 * Tests for the resource query language.
 */
public class ResourceQueryTest extends AbstractContextTest {
    @Test
    public void testAttrCISEq() throws Throwable {
        createTestResource();
        Resource rs2 = createTestResource().set("attr", "val1");
        Resource rs3 = createTestResource().set("attr", "Val1");
        createTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr like 'val1'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrCSEq() throws Throwable {
        createTestResource().set("attr", "val1");
        Resource rs2 = createTestResource().set("attr", "Val1");
        Resource rs3 = createTestResource().set("attr", "Val1");
        List<Resource> result = resourceManager.findResources("@attr eq 'Val1'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNull() throws Throwable {
        createTestResource().set("attr", "val1");
        Resource rs2 = createTestResource();
        Resource rs3 = createTestResource();
        List<Resource> result = resourceManager.findResources("@attr is null");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNotNull() throws Throwable {
        createTestResource();
        Resource rs2 = createTestResource().set("attr", "val1");
        Resource rs3 = createTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr is not null");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsEmpty() throws Throwable {
        createTestResource().set("attr", "val1");
        Resource rs2 = createTestResource().set("attr", "");
        Resource rs3 = createTestResource().set("attr", "");
        List<Resource> result = resourceManager.findResources("@attr is empty");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrIsNotEmpty() throws Throwable {
        createTestResource().set("attr", "");
        Resource rs2 = createTestResource().set("attr", "val1");
        Resource rs3 = createTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr is not empty");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrRegex1() throws Throwable {
        createTestResource().set("attr", "xxx");
        Resource rs2 = createTestResource().set("attr", "vol");
        Resource rs3 = createTestResource().set("attr", "val");
        Resource rs4 = createTestResource().set("attr", "vala");
        List<Resource> result = resourceManager.findResources("@attr regex 'v.l'");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test
    public void testAttrRegex2() throws Throwable {
        createTestResource().set("attr", "xxx");
        Resource rs2 = createTestResource().set("attr", "vol");
        Resource rs3 = createTestResource().set("attr", "val");
        createTestResource().set("attr", "vala");
        createTestResource().set("attr", "aval");
        createTestResource().set("attr", "avala");
        List<Resource> result = resourceManager.findResources("@attr regex '^v.l$'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrNotLike() throws Throwable {
        createTestResource().set("attr", "val1");
        createTestResource().set("attr", "Val1");
        Resource rs2 = createTestResource().set("attr", "val2");
        Resource rs3 = createTestResource().set("attr", "Val2");
        List<Resource> result = resourceManager.findResources("@attr not like 'val1'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testOr() throws Throwable {
        createTestResource("id1");
        Resource rs2 = createTestResource("id2");
        Resource rs3 = createTestResource("id3");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq 'id3'");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testOr2() throws Throwable {
        createTestResource("id1");
        Resource rs2 = createTestResource("id2");
        Resource rs3 = createTestResource("id3");
        Resource rs4 = createTestResource("id4");
        createTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq 'id3' or @id eq 'id4'");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test(dependsOnMethods = "testOr2")
    public void testStrings() throws Throwable {
        createTestResource("id1");
        Resource rs2 = createTestResource("id2");
        Resource rs3 = createTestResource("id3");
        Resource rs4 = createTestResource("id4");
        createTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq id3 or @id eq \"id4\"");
        assertContainsSame(result, rs2, rs3, rs4);
    }

    @Test(dependsOnMethods = "testOr2")
    public void testBadString() throws Throwable {
        createTestResource("id1");
        Resource rs2 = createTestResource("id2");
        Resource rs3 = createTestResource("id3");
        createTestResource("id4");
        createTestResource("id5");
        List<Resource> result = resourceManager.findResources("@id eq 'id2' or @id eq id3 or @id eq \"'id4\"");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAnd() throws Throwable {
        createTestResource().setUid("uid1");
        Resource rs2 = createTestResource("id2").set("attr1", "val1").set("attr2", "val2");
        createTestResource().set("uid", "uid3");
        List<Resource> result = resourceManager.findResources("@attr1 eq 'val1' and @attr2 eq 'val2'");
        assertContainsSame(result, rs2);
    }

    @Test
    public void testAnd2() throws Throwable {
        createTestResource().setUid("uid1");
        Resource rs2 = createTestResource("id2").set("attr1", "val1").set("attr2", "val2").set("attr3", "val3");
        createTestResource().set("uid", "uid3");
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
        createTestResource();
        execute();
        ChildOfScope impl = findJavaAction(ChildOfScope.class, parent);
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
        createTestResource();
        Resource parent = createTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        createChildTestResource(null, child2);
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("childof @id eq 'id'");
        assertContainsSame(childs, child1, child2);
    }

    @Test
    public void testChildOfRecursiveParam() throws Throwable {
        createTestResource();
        Resource parent = createTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        Resource child3 = createChildTestResource(null, child2);
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("childof* @id eq 'id'");
        assertContainsSame(childs, child1, child2, child3);
    }

    @Test
    public void testFindByType() throws Throwable {
        register(FindByType.class, "findbytype");
        createTestResource();
        Resource r1 = resourceManager.createResource("test:findbytype");
        Resource r2 = resourceManager.createResource("test:findbytype");
        createTestResource();
        execute();
        List<Resource> resources = ctx.findResources("type test:findbytype");
        assertContainsSame(resources, r1, r2);
    }

    public static class FindByType {
    }
}
