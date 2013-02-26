/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package antlrsandbox;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class Test {
    public static void main(String[] args) {
        parse("bla.foo:bar { 'asfdfdsa' }");
        parse("bla.foo:bar { 'asfdfdsa': }");
        parse("bla.foo:bar { attr = 'val' }");
        parse("bla.foo:bar { foo.bar:bla {} }");
        parse("bla.foo:bar { 'testid': attr = 'val' }");
        parse("bla.foo:bar { 'testid': attr = 'val', attr2 = 'val2' }");
        parse("bla.foo:bar { 'asfdfdsa':; 'bla':; }");
        parse("bla.foo:bar { 'asfdfdsa': attr = 'val'; 'bla':; }");
        parse("bla.foo:bar { 'asfdfdsa': attr = 'val', bla.ga:urg {'asdfsfda':} , rsfa {'fewfqwef':}; 'bla':; }");
    }

    private static void parse(String s) {
        System.out.println("Parsing "+s);
        ANTLRInputStream input = new ANTLRInputStream(s);
        TestLexer lexer = new TestLexer(input);
        TestParser parser = new TestParser(new CommonTokenStream(lexer));
        TestParser.StartContext start = parser.start();
    }
}
