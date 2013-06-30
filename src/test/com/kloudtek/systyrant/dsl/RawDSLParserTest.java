/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.context.RequiresExpression;
import com.kloudtek.systyrant.context.ResourceImpl;
import com.kloudtek.systyrant.context.ResourceMatcher;
import com.kloudtek.systyrant.dsl.statement.CreateResourceStatement;
import com.kloudtek.systyrant.dsl.statement.InvokeMethodStatement;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.tools.ant.util.ReflectUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Those tests check {@link DSLScript} objects produced by DSL code snippets.
 */
public class RawDSLParserTest {
    private ArrayList<RawDSLParserTester> testers = new ArrayList<>();
    private STContext context;
    private DSLParser parser = new AntlrDSLParser();

    public RawDSLParserTest() throws InvalidResourceDefinitionException, STRuntimeException {
        context = new STContext();
    }

    @AfterMethod
    public void verifyTestersRan() {
        synchronized (testers) {
            for (RawDSLParserTester tester : testers) {
                assertTrue(tester.isExecuted(), "Found unexecuted tester");
            }
            testers.clear();
        }
    }

    @Test
    public void testCreateResource() throws InvalidScriptException, STRuntimeException {
        parse("foo {}").dec("foo").run();
        parse("def {}").dec("def").run();
    }

    @Test
    public void testCreateResourceFQN() throws InvalidScriptException, STRuntimeException {
        parse("foo.bar {}").dec("foo.bar").run();
        parse("foo.bar.baz {}").dec("foo.bar.baz").run();
    }

//    @Test
//    public void testSingleCreateResourceWithId() throws InvalidScriptException, STRuntimeException {
//        parse("foo:bar { 'hello': }").dec("foo:bar").run();
//    }

    @Test(expectedExceptions = InvalidScriptException.class, expectedExceptionsMessageRegExp = "\\[1:7] unexpected token: :")
    public void testInvalidDefine() throws InvalidScriptException, STRuntimeException {
        parse("def bar:foo {}").run();
    }

    @Test(expectedExceptions = InvalidScriptException.class, expectedExceptionsMessageRegExp = "\\[1:5] unexpected token: \\)")
    public void testInvalidCreateResource() throws InvalidScriptException, STRuntimeException {
        parse("foo {)");
    }

    @Test(expectedExceptions = InvalidScriptException.class, expectedExceptionsMessageRegExp = "\\[1:5] unexpected token: <EOF>")
    public void testInvalidCreateResource2() throws InvalidScriptException, STRuntimeException {
        parse("foo {");
    }

    @Test
    public void testSimpleDefResource() throws InvalidScriptException, STRuntimeException {
        parse("def foo {}").def("foo").run();
    }

    @Test
    public void testImports() throws InvalidScriptException {
        parse("import foo;").imports("foo").run();
        parse("import import;").imports("import").run();
        parse("import def;").imports("def").run();
        parse("import foo; import bar;").imports("foo", "bar").run();
        parse("import foo:bar;").imports("foo:bar").run();
        parse("import foo.bar:baz;").imports("foo.bar:baz").run();
    }

    @Test(expectedExceptions = InvalidScriptException.class, expectedExceptionsMessageRegExp = "\\[1:10] unexpected token: <EOF>")
    public void testInvalidImport() throws InvalidScriptException, STRuntimeException {
        parse("import foo");
    }

    @Test
    public void testDefineWithSingleSimpleCreateEl() throws InvalidScriptException, STRuntimeException {
        DSLScript script = parser.parse(context, "def test { test2 { 'test': } }");
        assertEquals(script.getDefines().size(), 1);
        DSLResourceDefinition resourceDefStatement = script.getDefines().get(0);
        List<Statement> pst = resourceDefStatement.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateResourceStatement);
        CreateResourceStatement createEl = (CreateResourceStatement) st;
        validateResource(createEl, "test2", 1);
        validateResourceInstance(createEl, 0, "test");
    }

    @Test
    public void testDefineWithSingleCreateElWithParams() throws InvalidScriptException, STRuntimeException {
        DSLScript script = parser.parse(context, "def test { test2 { 'tval': attr1=\"test\", attr2=22 , attr = 'value', attr3 = uid } }");
        assertEquals(script.getDefines().size(), 1);
        DSLResourceDefinition resourceDefStatement = script.getDefines().get(0);
        List<Statement> pst = resourceDefStatement.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateResourceStatement);
        CreateResourceStatement createEl = (CreateResourceStatement) st;
        validateResource(createEl, "test2", 1);
        validateResourceInstance(createEl, 0, "tval", "attr1", "test", "attr2", "22", "attr", "value", "attr3", "uid");
    }

    @Test
    public void testDefineWithSingleFQNCreateEl() throws InvalidScriptException {
        DSLScript script = parser.parse(context, "def test { foo.bar.test2 {} }");
        assertEquals(script.getDefines().size(), 1);
        DSLResourceDefinition resourceDefStatement = script.getDefines().get(0);
        List<Statement> pst = resourceDefStatement.getStatementsForStage(Stage.PREPARE);
        assertEquals(pst.size(), 1);
        Statement st = pst.get(0);
        assertTrue(st instanceof CreateResourceStatement);
        CreateResourceStatement createEl = (CreateResourceStatement) st;
        validateResource(createEl, "foo.bar.test2", 1);
    }

    @Test
    public void testSimpleCreateElementWithFQName() throws InvalidScriptException, STRuntimeException {
        DSLScript script = parser.parse(context, "foo.bar.bla { 'dfsa' }");
        validateStatements(script, CreateResourceStatement.class);
        validateResourceInstance(script, "foo.bar", "bla", 0, 0, "dfsa");
    }

    @Test
    public void testInvokeMethod() throws InvalidScriptException, STRuntimeException {
        DSLScript script = parser.parse(context, "invokemethod('bla',\"bazz\",'xx',foo='bar',baz=bla)");
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

    @Test
    public void testParseRequiresExpression() throws STRuntimeException, InvalidResourceDefinitionException {
        STContext ctx = new STContext();
        Resource res = new ResourceImpl(ctx, null, null, null, null);
        RequiresExpression requiresExpression = new RequiresExpression(res, "test.val( bla = 'asd', ba=\"asdffdsa\", adsf=sfafdsa ), asfd.asds, foobar( x = 'z' )");
        ArrayList<RequiresExpression.RequiredDependency> deps = (ArrayList<RequiresExpression.RequiredDependency>) ReflectUtil.getField(requiresExpression, "requiredDependencies");
        assertEquals(deps.size(), 3);
        assertEquals(deps.get(0).getName().toString(), "test.val");
        assertEquals(deps.get(0).getAttrs().size(), 3);
        assertEquals(deps.get(0).getAttrs().get("bla").getRawValue(), "asd");
        assertEquals(deps.get(0).getAttrs().get("ba").getRawValue(), "asdffdsa");
        assertEquals(deps.get(0).getAttrs().get("adsf").getRawValue(), "sfafdsa");
        assertEquals(deps.get(1).getName().toString(), "asfd.asds");
        assertEquals(deps.get(1).getAttrs().size(), 0);
        assertEquals(deps.get(2).getName().toString(), "foobar");
        assertEquals(deps.get(2).getAttrs().size(), 1);
        assertEquals(deps.get(2).getAttrs().get("x").getRawValue(), "z");
    }

    private RawDSLParserTester parse(String script) {
        RawDSLParserTester tester = new RawDSLParserTester(AntlrDSLParser.createParser(script).script());
        synchronized (testers) {
            testers.add(tester);
        }
        return tester;
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

    private void validateParams(HashMap<String, Map<String, Parameter>> p, String id, String... params) throws STRuntimeException {
        for (int i = 0; i < params.length; i += 2) {
            assertEquals(p.get(id).get(params[i]).eval(null, null), params[i + 1]);
        }
    }

    private static void validateTotalStatements(DSLScript script, int maxStatements) {
        assertEquals(script.getStatements().size(), maxStatements);
    }

    private static void validateResource(CreateResourceStatement createEl, String fqname, int instanceCount) {
        assertEquals(createEl.getType(), fqname != null ? new FQName(fqname) : fqname);
        assertEquals(createEl.getInstances().size(), instanceCount);
    }

    private void validateResourceInstance(DSLScript script, String pkg, String name, int idx, int instanceNb, String id, String... params) throws STRuntimeException {
        CreateResourceStatement statement = (CreateResourceStatement) script.getStatements().get(idx);
        assertEquals(statement.getType().getPkg(), pkg);
        assertEquals(statement.getType().getName(), name);
        validateResourceInstance(statement, instanceNb, id, params);
    }

    private void validateResourceInstance(CreateResourceStatement createEl, int instanceNb, String id, String... params) throws STRuntimeException {
        CreateResourceStatement.Instance instance = createEl.getInstances().get(instanceNb);
        if (id == null) {
            assertNull(instance.getId());
        } else {
            assertEquals(instance.getId(), id);
        }
        if (params != null) {
            for (int i = 0; i < params.length; i += 2) {
                String eval = instance.getAttrAssignment(params[i]).eval(null, null);
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
}
