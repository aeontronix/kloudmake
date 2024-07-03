/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.AbstractContextTest;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.annotation.Execute;
import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * Tests for the resource query language.
 */
public class DSLQueryLangTests extends AbstractContextTest {
    private Field field;

    @Test
    public void testIdMatchNoScope() throws Throwable {
        Resource rs1 = createTestResource("parent1");
        Resource rs2 = createChildTestResource("someid", rs1);
        Resource rs3 = createTestResource("parent2");
        Resource rs4 = createChildTestResource("someid", rs3);
        Resource rs5 = createChildTestResource("someid", rs4);
        List<Resource> result = resourceManager.findResources("someid", rs3);
        assertContainsSame(result, rs4, rs5);
    }

    @Test
    public void testIdMatch2NoScope() throws Throwable {
        Resource rs1 = createTestResource("parent1");
        Resource rs2 = createChildTestResource("someid", rs1);
        Resource rs3 = createTestResource("parent2");
        Resource rs4 = createChildTestResource("someid", rs3);
        List<Resource> result = resourceManager.findResources("someid");
        assertContainsSame(result, rs2, rs4);
    }

    @Test
    public void testIdMatchInScope() throws Throwable {
        Resource rs1 = createTestResource("parent1");
        Resource rs2 = createChildTestResource("someid", rs1);
        Resource rs3 = createTestResource("parent2");
        Resource rs4 = createChildTestResource("someid", rs3);
        List<Resource> result = resourceManager.findResources("someid", rs3);
        assertContainsSame(result, rs4);
    }

    @Test
    public void testUidMatchFromRoot() throws Throwable {
        Resource rs1 = createTestResource("parent1");
        Resource rs2 = createChildTestResource("someid", rs1);
        Resource rs3 = createTestResource("a");
        Resource rs4 = createChildTestResource("b", rs3);
        Resource rs5 = createChildTestResource("c", rs4);
        List<Resource> result = resourceManager.findResources("a.b.c");
        assertContainsSame(result, rs5);
    }

    @Test
    public void testUidMatchFromResource() throws Throwable {
        Resource rs1 = createTestResource("parent1");
        Resource rs2 = createChildTestResource("someid", rs1);
        Resource rs3 = createTestResource("a");
        Resource rs4 = createChildTestResource("b", rs3);
        Resource rs5 = createChildTestResource("c", rs4);
        Resource rs6 = createChildTestResource("d", rs5);
        List<Resource> result = resourceManager.findResources("b.c.d", rs3);
        assertContainsSame(result, rs6);
    }

    @Test
    public void testUIdMatchUsingDepends() throws Throwable {
        Resource rs1 = createTestResource("a");
        Resource rs2 = createChildTestResource("someid", rs1);
        rs2.addDependency("b.c.d");
        Resource rs3 = createChildTestResource("b", rs1);
        Resource rs4 = createChildTestResource("c", rs3);
        Resource rs5 = createChildTestResource("d", rs4);
        execute();
        assertContainsSame(rs2.getDependencies(), rs1, rs5);
    }

    public static void main(String[] args) {
        AeonBuildLangParser parser = new AeonBuildLangParser(new CommonTokenStream(new AeonBuildLangLexer(new ANTLRInputStream("parent1.as.someid"))));
        AeonBuildLangParser.QueryContext queryUidMatchContext = parser.query();
        System.out.println(queryUidMatchContext.getText());

    }

    @Test
    public void testAttrCISEq() throws Throwable {
        createTestResource();
        Resource rs2 = createTestResource().set("attr", "val1");
        Resource rs3 = createTestResource().set("attr", "Val1");
        createTestResource().set("attr", "val2");
        List<Resource> result = resourceManager.findResources("@attr like 'val1'");
        assertContainsSame(result, rs2, rs3);
        List<Resource> result2 = resourceManager.findResources("@attr = 'val1'");
        assertContainsSame(result2, rs2, rs3);
    }

    @Test
    public void testAttrGt() throws Throwable {
        createTestResource();
        createTestResource().set("attr", "2");
        Resource rs2 = createTestResource().set("attr", "22");
        Resource rs3 = createTestResource().set("attr", "20");
        createTestResource().set("attr", "12");
        List<Resource> result = resourceManager.findResources("@attr gt 15");
        assertContainsSame(result, rs2, rs3);
        resourceManager.findResources("@attr > 15");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrGt2() throws Throwable {
        createTestResource();
        createTestResource().set("attr", "d");
        Resource rs2 = createTestResource().set("attr", "x");
        Resource rs3 = createTestResource().set("attr", "z");
        createTestResource().set("attr", "b");
        List<Resource> result = resourceManager.findResources("@attr gt f");
        assertContainsSame(result, rs2, rs3);
        result = resourceManager.findResources("@attr > f");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrLt() throws Throwable {
        createTestResource().set("attr", "100");
        Resource rs1 = createTestResource();
        Resource rs2 = createTestResource().set("attr", "22");
        createTestResource().set("attr", "120");
        List<Resource> result = resourceManager.findResources("@attr lt 50");
        assertContainsSame(result, rs1, rs2);
        result = resourceManager.findResources("@attr < 50");
        assertContainsSame(result, rs1, rs2);
    }

    @Test
    public void testAttrLt2() throws Throwable {
        createTestResource().set("attr", "y");
        Resource rs2 = createTestResource();
        Resource rs3 = createTestResource().set("attr", "a");
        createTestResource().set("attr", "z");
        List<Resource> result = resourceManager.findResources("@attr lt f");
        assertContainsSame(result, rs2, rs3);
        result = resourceManager.findResources("@attr < f");
        assertContainsSame(result, rs2, rs3);
    }

    @Test
    public void testAttrCSEq() throws Throwable {
        createTestResource().set("attr", "val1");
        Resource rs2 = createTestResource().set("attr", "Val1");
        Resource rs3 = createTestResource().set("attr", "Val1");
        List<Resource> result = resourceManager.findResources("@attr eq 'Val1'");
        assertContainsSame(result, rs2, rs3);
        result = resourceManager.findResources("@attr == 'Val1'");
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
        result = resourceManager.findResources("@attr ~= 'v.l'");
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
        result = resourceManager.findResources("@attr ~= '^v.l$'");
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
        createTestResource().set("attr1", "val1");
        Resource rs2 = createTestResource("id2").set("attr1", "val1").set("attr2", "val2");
        createTestResource().set("attr2", "val2");
        List<Resource> result = resourceManager.findResources("@attr1 eq 'val1' and @attr2 eq 'val2'");
        assertContainsSame(result, rs2);
    }

    @Test
    public void testAnd2() throws Throwable {
        createTestResource().set("attr1", "val1");
        Resource rs2 = createTestResource("id2").set("attr1", "val1").set("attr2", "val2").set("attr3", "val3");
        createTestResource().set("attr3", "val3");
        List<Resource> result = resourceManager.findResources("@attr1 eq 'val1' and @attr2 eq 'val2' and @attr3 eq 'val3'");
        assertContainsSame(result, rs2);
    }

    @Test
    public void testChildOfScope() throws Throwable {
        register(ChildOfScope.class, "childofscope");
        Resource parent = resourceManager.createResource("test.childofscope");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        createChildTestResource(null, child2);
        createTestResource();
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
            BuildContextImpl ctx = BuildContextImpl.get();
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
    public void testBrackets1() throws Throwable {
        createTestResource();
        Resource r1 = createTestResource("r1").set("k", "1");
        Resource r2 = createTestResource("r2").set("k", "2");
        Resource r3 = createTestResource("r3");
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("( @id eq 'r1' or @id eq 'r2' ) and @k eq '2'");
        assertContainsSame(childs, r2);
    }

    @Test
    public void testBrackets2() throws Throwable {
        createTestResource();
        Resource r1 = createTestResource("r1").set("k", "1");
        Resource r2 = createTestResource("r2").set("k", "2");
        Resource r3 = createTestResource("r3");
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("@id eq 'r1' or ( @id eq 'r2' and @k eq '2' )");
        assertContainsSame(childs, r1, r2);
    }

    @Test(enabled = false)
    public void testDepOfScope() throws Throwable {
        register(DepOfScope.class, "depsofscope");
        Resource r1 = resourceManager.createResource("test.depsofscope");
        Resource r2 = createTestResource();
        r2.addDependency(r1);
        Resource r3 = createTestResource();
        r3.addDependency(r1);
        createTestResource().addDependency(r3);
        createTestResource();
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
            BuildContextImpl ctx = BuildContextImpl.get();
            found = ctx.findResources("depends");
        }
    }

    @Test(enabled = false)
    public void testDepOfParam() throws Throwable {
        createTestResource();
        Resource res1 = createTestResource("id");
        Resource res2 = createTestResource();
        res2.addDependency(res1);
        Resource res3 = createTestResource();
        res3.addDependency(res3);
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("depends @id eq 'id'");
        assertContainsSame(childs, res2, res3);
    }

    @Test(enabled = false)
    public void testDepOfRecursiveParam() throws Throwable {
        createTestResource();
        Resource parent = createTestResource("id");
        Resource child1 = createChildTestResource(null, parent);
        Resource child2 = createChildTestResource(null, parent);
        Resource child3 = createChildTestResource(null, child2);
        createTestResource();
        execute();
        List<Resource> childs = ctx.findResources("depends* @id eq 'id'");
        assertContainsSame(childs, child1, child2, child3);
    }

    @Test
    public void testFindByType() throws Throwable {
        register(FindByType.class, "findbytype");
        createTestResource();
        Resource r1 = resourceManager.createResource("test.findbytype");
        Resource r2 = resourceManager.createResource("test.findbytype");
        createTestResource();
        execute();
        List<Resource> resources = ctx.findResources("type test.findbytype");
        assertContainsSame(resources, r1, r2);
    }

    public static class FindByType {
    }
}
