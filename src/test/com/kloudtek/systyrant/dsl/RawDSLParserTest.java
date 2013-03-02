/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.dsl.statement.CreateElementsStatement;
import com.kloudtek.systyrant.dsl.statement.InvokeMethodStatement;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidVariableException;
import com.kloudtek.systyrant.resource.ResourceMatcher;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Those tests check {@link DSLScript} objects produced by DSL code snippets.
 */
public class RawDSLParserTest {
    private DSLParser parser = new AntlrDSLParser();

    @Test
    public void testSimpleImport() throws InvalidScriptException {
        DSLScript script = parser.parse("import foo;\n");
        checkImports(script, "foo");
    }

    @Test(expectedExceptions = InvalidScriptException.class)
    public void testSimpleImportMissingSc() throws InvalidScriptException {
        parser.parse("import foo\n");
    }

    @Test
    public void testTwoImport() throws InvalidScriptException {
        DSLScript script = parser.parse("import foo;\n\nimport bar;");
        checkImports(script, "foo", "bar");
    }

    @Test
    public void testFqImport() throws InvalidScriptException {
        DSLScript script = parser.parse("import foo.bar;");
        checkImports(script, "foo.bar");
    }

    @Test
    public void testSimpleDef() throws InvalidScriptException {
        DSLScript script = parser.parse("def test {}");
        assertEquals(script.getDefines().size(), 1);
        ResourceDefinition def = script.getDefines().get(0);
        assertEquals(def.getName(), "test");
    }

    @Test
    public void testDefineWithSingleSimpleCreateEl() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("def test { new test2 { 'test'=> } }");
        assertEquals(script.getDefines().size(), 1);
        ResourceDefinition resourceDefinition = script.getDefines().get(0);
        List<Statement> pst = resourceDefinition.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateElementsStatement);
        CreateElementsStatement createEl = (CreateElementsStatement) st;
        validateResource(createEl, "test2", 1);
        validateResourceInstance(createEl, 0, "test");
    }

    @Test
    public void testDefineWithSingleCreateElWithParams() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("def test { new test2 { 'tval'=> attr1=\"test\", attr2=22 , attr = 'value' } }");
        assertEquals(script.getDefines().size(), 1);
        ResourceDefinition resourceDefinition = script.getDefines().get(0);
        List<Statement> pst = resourceDefinition.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateElementsStatement);
        CreateElementsStatement createEl = (CreateElementsStatement) st;
        validateResource(createEl, "test2", 1);
        validateResourceInstance(createEl, 0, "tval", "attr1", "test", "attr2", "22", "attr", "value");
    }

    @Test
    public void testDefineWithSingleFQNCreateEl() throws InvalidScriptException {
        DSLScript script = parser.parse("def test { new foo.bar:test2 {} }");
        assertEquals(script.getDefines().size(), 1);
        ResourceDefinition resourceDefinition = script.getDefines().get(0);
        List<Statement> pst = resourceDefinition.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateElementsStatement);
        CreateElementsStatement createEl = (CreateElementsStatement) st;
        validateResource(createEl, "foo.bar:test2", 1);
    }

    @Test
    public void testSimpleCreateElement() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("new package { 'dfsa' }");
        validateStatements(script, CreateElementsStatement.class);
        validateResourceInstance(script, null, "package", 0, 0, "dfsa");
    }

    @Test
    public void testSimpleCreateElementWithFQName() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("new foo.bar:bla { 'dfsa' }");
        validateStatements(script, CreateElementsStatement.class);
        validateResourceInstance(script, "foo.bar", "bla", 0, 0, "dfsa");
    }

    @Test
    public void testCreateElementWithFQNameImport() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("new foo.bar:import { 'dfsa' }");
        validateStatements(script, CreateElementsStatement.class);
        validateResourceInstance(script, "foo.bar", "import", 0, 0, "dfsa");
    }

    @Test
    public void testInvokeMethod() throws InvalidScriptException, InvalidVariableException {
        DSLScript script = parser.parse("invokemethod('bla',\"bazz\",'xx',foo='bar',baz=bla)");
        validateStatements(script, InvokeMethodStatement.class);
        InvokeMethodStatement statement = (InvokeMethodStatement) script.getStatements().get(0);
        assertEquals(statement.getMethodName(), "invokemethod");
        assertEquals(statement.getParameters().size(), 5);
        List<Parameter> p = statement.getParameters().getParameters();
        assertEquals(p.size(), 3);
        assertEquals(p.get(0).eval(null, null), "bla");
        assertEquals(p.get(1).eval(null, null), "bazz");
        assertEquals(p.get(2).eval(null, null), "xx");
        Map<String, Parameter> np = statement.getParameters().getNamedParameters();
        assertEquals(np.size(), 2);
        assertEquals(np.get("foo").eval(null, null), "bar");
        assertEquals(np.get("baz").eval(null, null), "bla");
    }

    private void checkImports(DSLScript script, String... imports) {
        List<ResourceMatcher> list = script.getImports();
        assertEquals(list.size(), imports.length);
        for (int i = 0; i < imports.length; i++) {
            ResourceMatcher resourceMatcher = list.get(i);
            StringBuilder tmp = new StringBuilder(resourceMatcher.getPkg());
            if (resourceMatcher.getName() != null) {
                tmp.append(":").append(resourceMatcher.getName());
            }
            assertEquals(tmp.toString(), imports[i]);
        }
    }

    private void validateParams(HashMap<String, Map<String, Parameter>> p, String id, String... params) throws InvalidVariableException {
        for (int i = 0; i < params.length; i += 2) {
            assertEquals(p.get(id).get(params[i]).eval(null, null), params[i + 1]);
        }
    }

    private static void validateTotalStatements(DSLScript script, int maxStatements) {
        assertEquals(script.getStatements().size(), maxStatements);
    }

    private static void validateResource(CreateElementsStatement createEl, String fqname, int instanceCount) {
        assertEquals(createEl.getElementName(), fqname != null ? new FQName(fqname) : fqname);
        assertEquals(createEl.getInstances().size(), instanceCount);
    }

    private void validateResourceInstance(DSLScript script, String pkg, String name, int idx, int instanceNb, String id, String... params) throws InvalidVariableException {
        CreateElementsStatement statement = (CreateElementsStatement) script.getStatements().get(idx);
        assertEquals(statement.getElementName().getPkg(), pkg);
        assertEquals(statement.getElementName().getName(), name);
        validateResourceInstance(statement, instanceNb, id, params);
    }

    private void validateResourceInstance(CreateElementsStatement createEl, int instanceNb, String id, String... params) throws InvalidVariableException {
        CreateElementsStatement.Instance instance = createEl.getInstances().get(instanceNb);
        if (id == null) {
            assertNull(instance.getId());
        } else {
            assertEquals(instance.getId().getRawValue(), id);
        }
        if (params != null) {
            for (int i = 0; i < params.length; i += 2) {
                String eval = instance.getParameters().get(params[i]).eval(null, null);
                assertEquals(eval, params[i + 1]);
            }
        }
    }

    private void validateStatements(DSLScript script, Class<?>... createElementsStatementClass) {
        List<Statement> statements = script.getStatements();
        assertEquals(statements.size(), createElementsStatementClass.length);
        for (int i = 0; i < createElementsStatementClass.length; i++) {
            assertTrue(createElementsStatementClass[i].isInstance(statements.get(i)));
        }
    }


    //    }
//        validateResource(statement.getChildrens().get("test1").get(0), "foo.bar:test", 0, "test2");
//        assertEquals(statement.getChildrens().size(), 1);
//        CreateElementsStatement statement = validateResource(script, 0, 0, "foo.bar:test", "test1", "attr", "val");
//        validateTotalStatements(script, 1);
//        DSLScript script = parser.parse("foo.bar:test { 'test1': attr = 'val' { foo.bar:test {'test2'} } }");
//    public void testSimpleCreateElementWithChildren() throws InvalidScriptException {
//    }
//        validateResource(script, 0, 0, "foo.bar:test", "test", "attr1", "val", "attr2", "15");
//        validateTotalStatements(script, 1);
//        DSLScript script = parser.parse("foo.bar:test { 'test': attr1 = 'val', attr2 = 15 }");
//    public void testSimpleCreateElementWithAttr() throws InvalidScriptException {
//    @Test
//
//    }
//        validateResource(script, 0, 1, "foo.bar:test", "test2");
//        validateResource(script, 0, 0, "foo.bar:test", "test1");
//        validateTotalStatements(script, 1);
//        DSLScript script = parser.parse("foo.bar:test { 'test1': ; 'test2': ;}");
//    public void testSimpleCreateTwoElementInlineDeclarations() throws InvalidScriptException {
//    @Test
//
//    }
//        validateResource(script, 1, 0, "foo.bar:test", "test2");
//        validateResource(script, 0, 0, "foo.bar:test", "test1");
//        validateTotalStatements(script, 2);
//        DSLScript script = parser.parse("foo.bar:test { 'test1' }\nfoo.bar:test { 'test2' }");
//    public void testSimpleCreateTwoElementMultipleDeclarations() throws InvalidScriptException {
}
