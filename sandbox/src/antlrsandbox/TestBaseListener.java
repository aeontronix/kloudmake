/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package antlrsandbox;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ErrorNode;

public class TestBaseListener implements TestListener {
	@Override public void enterCreateResource(TestParser.CreateResourceContext ctx) { }
	@Override public void exitCreateResource(TestParser.CreateResourceContext ctx) { }

	@Override public void enterFullyQualifiedId(TestParser.FullyQualifiedIdContext ctx) { }
	@Override public void exitFullyQualifiedId(TestParser.FullyQualifiedIdContext ctx) { }

	@Override public void enterCreateResourceInstanceParam(TestParser.CreateResourceInstanceParamContext ctx) { }
	@Override public void exitCreateResourceInstanceParam(TestParser.CreateResourceInstanceParamContext ctx) { }

	@Override public void enterPackageName(TestParser.PackageNameContext ctx) { }
	@Override public void exitPackageName(TestParser.PackageNameContext ctx) { }

	@Override public void enterCreateResourceInstanceChild(TestParser.CreateResourceInstanceChildContext ctx) { }
	@Override public void exitCreateResourceInstanceChild(TestParser.CreateResourceInstanceChildContext ctx) { }

	@Override public void enterStart(TestParser.StartContext ctx) { }
	@Override public void exitStart(TestParser.StartContext ctx) { }

	@Override public void enterCreateResourceInstanceElements(TestParser.CreateResourceInstanceElementsContext ctx) { }
	@Override public void exitCreateResourceInstanceElements(TestParser.CreateResourceInstanceElementsContext ctx) { }

	@Override public void enterCreateResourceSingleInstance(TestParser.CreateResourceSingleInstanceContext ctx) { }
	@Override public void exitCreateResourceSingleInstance(TestParser.CreateResourceSingleInstanceContext ctx) { }

	@Override public void enterCreateResourceMultipleInstance(TestParser.CreateResourceMultipleInstanceContext ctx) { }
	@Override public void exitCreateResourceMultipleInstance(TestParser.CreateResourceMultipleInstanceContext ctx) { }

	@Override public void enterCreateResourceInstanceId(TestParser.CreateResourceInstanceIdContext ctx) { }
	@Override public void exitCreateResourceInstanceId(TestParser.CreateResourceInstanceIdContext ctx) { }

	@Override public void enterCreateResourceInstanceParamValue(TestParser.CreateResourceInstanceParamValueContext ctx) { }
	@Override public void exitCreateResourceInstanceParamValue(TestParser.CreateResourceInstanceParamValueContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext ctx) { }
	@Override public void exitEveryRule(ParserRuleContext ctx) { }
	@Override public void visitTerminal(TerminalNode node) { }
	@Override public void visitErrorNode(ErrorNode node) { }
}