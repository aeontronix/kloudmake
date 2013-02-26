/*
 * Copyright (c) 2013 KloudTek Ltd
 */

// $ANTLR ANTLRVersion> TestLexer.java generatedTimestamp>
package antlrsandbox;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TestLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__6=1, T__5=2, T__4=3, T__3=4, T__2=5, T__1=6, T__0=7, ID=8, NB=9, LF=10, 
		WS=11, UQSTRING=12, STRING=13;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'{'", "'.'", "','", "':'", "'='", "'}'", "';'", "ID", "NB", "'\n'", "WS", 
		"UQSTRING", "STRING"
	};
	public static final String[] ruleNames = {
		"T__6", "T__5", "T__4", "T__3", "T__2", "T__1", "T__0", "ID", "NB", "LF", 
		"WS", "UQSTRING", "STRING", "ESCAPE"
	};


	public TestLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Test.g"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 9: LF_action((RuleContext)_localctx, actionIndex); break;

		case 10: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1: skip();  break;
		}
	}
	private void LF_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\1\2\r\\\6\uffff\2\0\7\0\2\1\7\1\2\2\7\2\2\3\7\3\2\4\7\4\2\5\7\5\2\6\7"+
		"\6\2\7\7\7\2\b\7\b\2\t\7\t\2\n\7\n\2\13\7\13\2\f\7\f\2\r\7\r\1\0\1\0\1"+
		"\1\1\1\1\2\1\2\1\3\1\3\1\4\1\4\1\5\1\5\1\6\1\6\1\7\1\7\5\7.\b\7\n\7\f"+
		"\7\61\t\7\1\b\4\b\64\b\b\13\b\f\b\65\1\t\1\t\1\t\1\t\1\n\4\n=\b\n\13\n"+
		"\f\n>\1\n\1\n\1\13\1\13\1\f\1\f\1\f\5\fH\b\f\n\f\f\fK\t\f\1\f\1\f\1\f"+
		"\1\f\5\fQ\b\f\n\f\f\fT\t\f\1\f\1\f\3\fX\b\f\1\r\1\r\1\r\0\16\1\1\uffff"+
		"\3\2\uffff\5\3\uffff\7\4\uffff\t\5\uffff\13\6\uffff\r\7\uffff\17\b\uffff"+
		"\21\t\uffff\23\n\0\25\13\1\27\f\uffff\31\r\uffff\33\0\uffff\1\0\7\2AZ"+
		"az\3\609AZaz\2..\609\3\t\t\r\r  \b!!$&**--/9AZ^_az\2\"\"\\\\\2\'\'\\\\"+
		"c\0\1\1\0\0\0\0\3\1\0\0\0\0\5\1\0\0\0\0\7\1\0\0\0\0\t\1\0\0\0\0\13\1\0"+
		"\0\0\0\r\1\0\0\0\0\17\1\0\0\0\0\21\1\0\0\0\0\23\1\0\0\0\0\25\1\0\0\0\0"+
		"\27\1\0\0\0\0\31\1\0\0\0\1\35\1\0\0\0\3\37\1\0\0\0\5!\1\0\0\0\7#\1\0\0"+
		"\0\t%\1\0\0\0\13\'\1\0\0\0\r)\1\0\0\0\17+\1\0\0\0\21\63\1\0\0\0\23\67"+
		"\1\0\0\0\25<\1\0\0\0\27B\1\0\0\0\31W\1\0\0\0\33Y\1\0\0\0\35\36\5{\0\0"+
		"\36\2\1\0\0\0\37 \5.\0\0 \4\1\0\0\0!\"\5,\0\0\"\6\1\0\0\0#$\5:\0\0$\b"+
		"\1\0\0\0%&\5=\0\0&\n\1\0\0\0\'(\5}\0\0(\f\1\0\0\0)*\5;\0\0*\16\1\0\0\0"+
		"+/\7\0\0\0,.\7\1\0\0-,\1\0\0\0.\61\1\0\0\0/-\1\0\0\0/\60\1\0\0\0\60\20"+
		"\1\0\0\0\61/\1\0\0\0\62\64\7\2\0\0\63\62\1\0\0\0\64\65\1\0\0\0\65\63\1"+
		"\0\0\0\65\66\1\0\0\0\66\22\1\0\0\0\678\5\n\0\089\1\0\0\09:\6\t\0\0:\24"+
		"\1\0\0\0;=\7\3\0\0<;\1\0\0\0=>\1\0\0\0><\1\0\0\0>?\1\0\0\0?@\1\0\0\0@"+
		"A\6\n\1\0A\26\1\0\0\0BC\7\4\0\0C\30\1\0\0\0DI\5\"\0\0EH\3\33\r\0FH\b\5"+
		"\0\0GE\1\0\0\0GF\1\0\0\0HK\1\0\0\0IG\1\0\0\0IJ\1\0\0\0JL\1\0\0\0KI\1\0"+
		"\0\0LX\5\"\0\0MR\5\'\0\0NQ\3\33\r\0OQ\b\6\0\0PN\1\0\0\0PO\1\0\0\0QT\1"+
		"\0\0\0RP\1\0\0\0RS\1\0\0\0SU\1\0\0\0TR\1\0\0\0UX\5\'\0\0VX\3\27\13\0W"+
		"D\1\0\0\0WM\1\0\0\0WV\1\0\0\0X\32\1\0\0\0YZ\5\\\0\0Z[\t\0\0\0[\34\1\0"+
		"\0\0\n\0/\63\65>GIPRW";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
	    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}