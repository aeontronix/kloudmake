/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RawDSLParserTester implements Runnable {
    private boolean executed;
    private ArrayList<String> imports = new ArrayList<>();
    private ArrayList<String> expectedImports = new ArrayList<>();
    private HashMap<String, DeclareResource> decs = new HashMap<>();
    private HashMap<String, DeclareResource> expectedDecs = new HashMap<>();
    private HashMap<String, DefineResource> defs = new HashMap<>();
    private HashMap<String, DefineResource> expectedDefs = new HashMap<>();

    public RawDSLParserTester(SystyrantLangParser.ScriptContext scriptCtx) {
        for (SystyrantLangParser.StatementContext st : scriptCtx.statement()) {
            if (st.imp != null) {
                StringBuilder pkg = new StringBuilder(st.imp.pkg.getText());
                if (st.imp.type != null) {
                    pkg.append(':').append(st.imp.type.getText());
                }
                imports.add(pkg.toString());
            }
            if (st.create != null) {
                DeclareResource declareResource = new DeclareResource(st.create.type.getText());
                decs.put(declareResource.type, declareResource);
            }
            if (st.define != null) {
                DefineResource DefineResource = new DefineResource(st.define.type.getText());
                defs.put(DefineResource.type, DefineResource);
            }
        }
    }

    public void run() {
        Assert.assertEquals(imports, expectedImports);
        Assert.assertEquals(decs.keySet(), expectedDecs.keySet());
        Assert.assertEquals(defs.keySet(), expectedDefs.keySet());
        executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }

    public DeclareResource dec(String type) {
        DeclareResource declarationTester = new DeclareResource(type);
        expectedDecs.put(type, declarationTester);
        return declarationTester;
    }

    public DefineResource def(String type) {
        DefineResource declarationTester = new DefineResource(type);
        expectedDefs.put(type, declarationTester);
        return declarationTester;
    }

    public RawDSLParserTester imports(String... importValue) {
        expectedImports.addAll(Arrays.asList(importValue));
        return this;
    }

    public class DeclareResource {
        private String type;

        public DeclareResource(String type) {
            this.type = type;
        }

        public void run() {
            RawDSLParserTester.this.run();
        }
    }

    public class DefineResource {
        private String type;

        public DefineResource(String type) {
            this.type = type;
        }

        public void run() {
            RawDSLParserTester.this.run();
        }
    }
}
