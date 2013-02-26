/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package antlrsandbox;
import org.antlr.v4.runtime.tree.*;

public interface TestListener extends ParseTreeListener {
	void enterCreateResource(TestParser.CreateResourceContext ctx);
	void exitCreateResource(TestParser.CreateResourceContext ctx);

	void enterFullyQualifiedId(TestParser.FullyQualifiedIdContext ctx);
	void exitFullyQualifiedId(TestParser.FullyQualifiedIdContext ctx);

	void enterCreateResourceInstanceParam(TestParser.CreateResourceInstanceParamContext ctx);
	void exitCreateResourceInstanceParam(TestParser.CreateResourceInstanceParamContext ctx);

	void enterPackageName(TestParser.PackageNameContext ctx);
	void exitPackageName(TestParser.PackageNameContext ctx);

	void enterCreateResourceInstanceChild(TestParser.CreateResourceInstanceChildContext ctx);
	void exitCreateResourceInstanceChild(TestParser.CreateResourceInstanceChildContext ctx);

	void enterStart(TestParser.StartContext ctx);
	void exitStart(TestParser.StartContext ctx);

	void enterCreateResourceInstanceElements(TestParser.CreateResourceInstanceElementsContext ctx);
	void exitCreateResourceInstanceElements(TestParser.CreateResourceInstanceElementsContext ctx);

	void enterCreateResourceSingleInstance(TestParser.CreateResourceSingleInstanceContext ctx);
	void exitCreateResourceSingleInstance(TestParser.CreateResourceSingleInstanceContext ctx);

	void enterCreateResourceMultipleInstance(TestParser.CreateResourceMultipleInstanceContext ctx);
	void exitCreateResourceMultipleInstance(TestParser.CreateResourceMultipleInstanceContext ctx);

	void enterCreateResourceInstanceId(TestParser.CreateResourceInstanceIdContext ctx);
	void exitCreateResourceInstanceId(TestParser.CreateResourceInstanceIdContext ctx);

	void enterCreateResourceInstanceParamValue(TestParser.CreateResourceInstanceParamValueContext ctx);
	void exitCreateResourceInstanceParamValue(TestParser.CreateResourceInstanceParamValueContext ctx);
}