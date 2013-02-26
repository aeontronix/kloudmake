/*
 * Copyright (c) 2013 KloudTek Ltd
 */

// $ANTLR ANTLRVersion> TestParser.java generatedTimestamp>
package antlrsandbox;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TestParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__6=1, T__5=2, T__4=3, T__3=4, T__2=5, T__1=6, T__0=7, ID=8, NB=9, LF=10, 
		WS=11, UQSTRING=12, STRING=13;
	public static final String[] tokenNames = {
		"<INVALID>", "'{'", "'.'", "','", "':'", "'='", "'}'", "';'", "ID", "NB", 
		"'\n'", "WS", "UQSTRING", "STRING"
	};
	public static final int
		RULE_start = 0, RULE_createResource = 1, RULE_createResourceSingleInstance = 2, 
		RULE_createResourceMultipleInstance = 3, RULE_createResourceInstanceId = 4, 
		RULE_createResourceInstanceElements = 5, RULE_createResourceInstanceChild = 6, 
		RULE_createResourceInstanceParam = 7, RULE_createResourceInstanceParamValue = 8, 
		RULE_packageName = 9, RULE_fullyQualifiedId = 10;
	public static final String[] ruleNames = {
		"start", "createResource", "createResourceSingleInstance", "createResourceMultipleInstance", 
		"createResourceInstanceId", "createResourceInstanceElements", "createResourceInstanceChild", 
		"createResourceInstanceParam", "createResourceInstanceParamValue", "packageName", 
		"fullyQualifiedId"
	};

	@Override
	public String getGrammarFileName() { return "Test.g"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public TestParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StartContext extends ParserRuleContext {
		public CreateResourceContext createResource() {
			return getRuleContext(CreateResourceContext.class,0);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitStart(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22); createResource();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceContext extends ParserRuleContext {
		public CreateResourceMultipleInstanceContext createResourceMultipleInstance(int i) {
			return getRuleContext(CreateResourceMultipleInstanceContext.class,i);
		}
		public FullyQualifiedIdContext fullyQualifiedId() {
			return getRuleContext(FullyQualifiedIdContext.class,0);
		}
		public CreateResourceSingleInstanceContext createResourceSingleInstance() {
			return getRuleContext(CreateResourceSingleInstanceContext.class,0);
		}
		public List<CreateResourceMultipleInstanceContext> createResourceMultipleInstance() {
			return getRuleContexts(CreateResourceMultipleInstanceContext.class);
		}
		public CreateResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResource(this);
		}
	}

	public final CreateResourceContext createResource() throws RecognitionException {
		CreateResourceContext _localctx = new CreateResourceContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_createResource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24); fullyQualifiedId();
			setState(25); match(1);
			setState(33);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(26); createResourceSingleInstance();
				}
				break;

			case 2:
				{
				setState(30);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 7) | (1L << ID) | (1L << STRING))) != 0)) {
					{
					{
					setState(27); createResourceMultipleInstance();
					}
					}
					setState(32);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(35); match(6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceSingleInstanceContext extends ParserRuleContext {
		public List<CreateResourceInstanceElementsContext> createResourceInstanceElements() {
			return getRuleContexts(CreateResourceInstanceElementsContext.class);
		}
		public CreateResourceInstanceIdContext createResourceInstanceId() {
			return getRuleContext(CreateResourceInstanceIdContext.class,0);
		}
		public CreateResourceInstanceElementsContext createResourceInstanceElements(int i) {
			return getRuleContext(CreateResourceInstanceElementsContext.class,i);
		}
		public CreateResourceSingleInstanceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceSingleInstance; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceSingleInstance(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceSingleInstance(this);
		}
	}

	public final CreateResourceSingleInstanceContext createResourceSingleInstance() throws RecognitionException {
		CreateResourceSingleInstanceContext _localctx = new CreateResourceSingleInstanceContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_createResourceSingleInstance);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			_la = _input.LA(1);
			if (_la==STRING) {
				{
				setState(37); createResourceInstanceId();
				}
			}

			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(40); createResourceInstanceElements();
				}
				}
				setState(45);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceMultipleInstanceContext extends ParserRuleContext {
		public List<CreateResourceInstanceElementsContext> createResourceInstanceElements() {
			return getRuleContexts(CreateResourceInstanceElementsContext.class);
		}
		public CreateResourceInstanceIdContext createResourceInstanceId() {
			return getRuleContext(CreateResourceInstanceIdContext.class,0);
		}
		public CreateResourceInstanceElementsContext createResourceInstanceElements(int i) {
			return getRuleContext(CreateResourceInstanceElementsContext.class,i);
		}
		public CreateResourceMultipleInstanceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceMultipleInstance; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceMultipleInstance(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceMultipleInstance(this);
		}
	}

	public final CreateResourceMultipleInstanceContext createResourceMultipleInstance() throws RecognitionException {
		CreateResourceMultipleInstanceContext _localctx = new CreateResourceMultipleInstanceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_createResourceMultipleInstance);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			_la = _input.LA(1);
			if (_la==STRING) {
				{
				setState(46); createResourceInstanceId();
				}
			}

			setState(52);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(49); createResourceInstanceElements();
				}
				}
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(55); match(7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceInstanceIdContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(TestParser.STRING, 0); }
		public CreateResourceInstanceIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceInstanceId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceInstanceId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceInstanceId(this);
		}
	}

	public final CreateResourceInstanceIdContext createResourceInstanceId() throws RecognitionException {
		CreateResourceInstanceIdContext _localctx = new CreateResourceInstanceIdContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_createResourceInstanceId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57); match(STRING);
			setState(59);
			_la = _input.LA(1);
			if (_la==4) {
				{
				setState(58); match(4);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceInstanceElementsContext extends ParserRuleContext {
		public CreateResourceInstanceParamContext createResourceInstanceParam() {
			return getRuleContext(CreateResourceInstanceParamContext.class,0);
		}
		public CreateResourceInstanceChildContext createResourceInstanceChild() {
			return getRuleContext(CreateResourceInstanceChildContext.class,0);
		}
		public CreateResourceInstanceElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceInstanceElements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceInstanceElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceInstanceElements(this);
		}
	}

	public final CreateResourceInstanceElementsContext createResourceInstanceElements() throws RecognitionException {
		CreateResourceInstanceElementsContext _localctx = new CreateResourceInstanceElementsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_createResourceInstanceElements);
		try {
			setState(63);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(61); createResourceInstanceParam();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(62); createResourceInstanceChild();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceInstanceChildContext extends ParserRuleContext {
		public CreateResourceContext createResource() {
			return getRuleContext(CreateResourceContext.class,0);
		}
		public CreateResourceInstanceChildContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceInstanceChild; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceInstanceChild(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceInstanceChild(this);
		}
	}

	public final CreateResourceInstanceChildContext createResourceInstanceChild() throws RecognitionException {
		CreateResourceInstanceChildContext _localctx = new CreateResourceInstanceChildContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_createResourceInstanceChild);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65); createResource();
			setState(67);
			_la = _input.LA(1);
			if (_la==3) {
				{
				setState(66); match(3);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceInstanceParamContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(TestParser.ID, 0); }
		public CreateResourceInstanceParamValueContext createResourceInstanceParamValue() {
			return getRuleContext(CreateResourceInstanceParamValueContext.class,0);
		}
		public CreateResourceInstanceParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceInstanceParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceInstanceParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceInstanceParam(this);
		}
	}

	public final CreateResourceInstanceParamContext createResourceInstanceParam() throws RecognitionException {
		CreateResourceInstanceParamContext _localctx = new CreateResourceInstanceParamContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_createResourceInstanceParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); match(ID);
			setState(70); match(5);
			setState(71); createResourceInstanceParamValue();
			setState(73);
			_la = _input.LA(1);
			if (_la==3) {
				{
				setState(72); match(3);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateResourceInstanceParamValueContext extends ParserRuleContext {
		public TerminalNode NB() { return getToken(TestParser.NB, 0); }
		public TerminalNode STRING() { return getToken(TestParser.STRING, 0); }
		public CreateResourceInstanceParamValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createResourceInstanceParamValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterCreateResourceInstanceParamValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitCreateResourceInstanceParamValue(this);
		}
	}

	public final CreateResourceInstanceParamValueContext createResourceInstanceParamValue() throws RecognitionException {
		CreateResourceInstanceParamValueContext _localctx = new CreateResourceInstanceParamValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_createResourceInstanceParamValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			_la = _input.LA(1);
			if ( !(_la==NB || _la==STRING) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PackageNameContext extends ParserRuleContext {
		public TerminalNode ID(int i) {
			return getToken(TestParser.ID, i);
		}
		public List<TerminalNode> ID() { return getTokens(TestParser.ID); }
		public PackageNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterPackageName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitPackageName(this);
		}
	}

	public final PackageNameContext packageName() throws RecognitionException {
		PackageNameContext _localctx = new PackageNameContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_packageName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77); match(ID);
			setState(82);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==2) {
				{
				{
				setState(78); match(2);
				setState(79); match(ID);
				}
				}
				setState(84);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullyQualifiedIdContext extends ParserRuleContext {
		public PackageNameContext packageName() {
			return getRuleContext(PackageNameContext.class,0);
		}
		public TerminalNode ID() { return getToken(TestParser.ID, 0); }
		public FullyQualifiedIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullyQualifiedId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterFullyQualifiedId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitFullyQualifiedId(this);
		}
	}

	public final FullyQualifiedIdContext fullyQualifiedId() throws RecognitionException {
		FullyQualifiedIdContext _localctx = new FullyQualifiedIdContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_fullyQualifiedId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(85); packageName();
				setState(86); match(4);
				}
				break;
			}
			setState(90); match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\1\3\r]\2\0\7\0\2\1\7\1\2\2\7\2\2\3\7\3\2\4\7\4\2\5\7\5\2\6\7\6\2\7\7"+
		"\7\2\b\7\b\2\t\7\t\2\n\7\n\1\0\1\0\1\1\1\1\1\1\1\1\5\1\35\b\1\n\1\f\1"+
		" \t\1\3\1\"\b\1\1\1\1\1\1\2\3\2\'\b\2\1\2\5\2*\b\2\n\2\f\2-\t\2\1\3\3"+
		"\3\60\b\3\1\3\5\3\63\b\3\n\3\f\3\66\t\3\1\3\1\3\1\4\1\4\3\4<\b\4\1\5\1"+
		"\5\3\5@\b\5\1\6\1\6\3\6D\b\6\1\7\1\7\1\7\1\7\3\7J\b\7\1\b\1\b\1\t\1\t"+
		"\1\t\5\tQ\b\t\n\t\f\tT\t\t\1\n\1\n\1\n\3\nY\b\n\1\n\1\n\1\n\0\13\0\2\4"+
		"\6\b\n\f\16\20\22\24\0\1\2\t\t\r\r]\0\26\1\0\0\0\2\30\1\0\0\0\4&\1\0\0"+
		"\0\6/\1\0\0\0\b9\1\0\0\0\n?\1\0\0\0\fA\1\0\0\0\16E\1\0\0\0\20K\1\0\0\0"+
		"\22M\1\0\0\0\24X\1\0\0\0\26\27\3\2\1\0\27\1\1\0\0\0\30\31\3\24\n\0\31"+
		"!\5\1\0\0\32\"\3\4\2\0\33\35\3\6\3\0\34\33\1\0\0\0\35 \1\0\0\0\36\34\1"+
		"\0\0\0\36\37\1\0\0\0\37\"\1\0\0\0 \36\1\0\0\0!\32\1\0\0\0!\36\1\0\0\0"+
		"\"#\1\0\0\0#$\5\6\0\0$\3\1\0\0\0%\'\3\b\4\0&%\1\0\0\0&\'\1\0\0\0\'+\1"+
		"\0\0\0(*\3\n\5\0)(\1\0\0\0*-\1\0\0\0+)\1\0\0\0+,\1\0\0\0,\5\1\0\0\0-+"+
		"\1\0\0\0.\60\3\b\4\0/.\1\0\0\0/\60\1\0\0\0\60\64\1\0\0\0\61\63\3\n\5\0"+
		"\62\61\1\0\0\0\63\66\1\0\0\0\64\62\1\0\0\0\64\65\1\0\0\0\65\67\1\0\0\0"+
		"\66\64\1\0\0\0\678\5\7\0\08\7\1\0\0\09;\5\r\0\0:<\5\4\0\0;:\1\0\0\0;<"+
		"\1\0\0\0<\t\1\0\0\0=@\3\16\7\0>@\3\f\6\0?=\1\0\0\0?>\1\0\0\0@\13\1\0\0"+
		"\0AC\3\2\1\0BD\5\3\0\0CB\1\0\0\0CD\1\0\0\0D\r\1\0\0\0EF\5\b\0\0FG\5\5"+
		"\0\0GI\3\20\b\0HJ\5\3\0\0IH\1\0\0\0IJ\1\0\0\0J\17\1\0\0\0KL\7\0\0\0L\21"+
		"\1\0\0\0MR\5\b\0\0NO\5\2\0\0OQ\5\b\0\0PN\1\0\0\0QT\1\0\0\0RP\1\0\0\0R"+
		"S\1\0\0\0S\23\1\0\0\0TR\1\0\0\0UV\3\22\t\0VW\5\4\0\0WY\1\0\0\0XU\1\0\0"+
		"\0XY\1\0\0\0YZ\1\0\0\0Z[\5\b\0\0[\25\1\0\0\0\f\36!&+/\64;?CIRX";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
	    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}