/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import org.antlr.v4.runtime.Token;

import java.util.*;

import static com.kloudtek.systyrant.dsl.AntLRUtils.nullToEmpty;

public class Parameters {
    private ArrayList<Parameter> parameters = new ArrayList<>();
    private HashMap<String, Parameter> namedParameters;

    public synchronized List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public synchronized Map<String, Parameter> getNamedParameters() {
        return Collections.unmodifiableMap(namedParameters);
    }

    public synchronized void addParameter(Token location, Parameter parameter) throws InvalidScriptException {
        if (namedParameters != null) {
            throw new InvalidScriptException("Unnamed parameters must not be present after named ones",
                    "[" + location.getLine() + ":" + location.getCharPositionInLine(), null, null);
        }
        parameters.add(parameter);
    }

    public synchronized void addNamedParameter(String name, Parameter parameter) throws InvalidScriptException {
        if (namedParameters == null) {
            namedParameters = new HashMap<>();
        }
        namedParameters.put(name, parameter);
    }

    public int size() {
        int size = parameters.size();
        if (namedParameters != null) {
            size += namedParameters.size();
        }
        return size;
    }

    public static Map<String, Parameter> assignmentToMap(List<SystyrantLangParser.ParameterAssignmentContext> parameterAssignmentContexts) {
        HashMap<String, Parameter> map = new HashMap<>();
        for (SystyrantLangParser.ParameterAssignmentContext parameterAssignmentContext : nullToEmpty(parameterAssignmentContexts)) {
            String attrName = parameterAssignmentContext.anyId().getText();
            SystyrantLangParser.StaticOrDynamicValueContext staticOrDynamicValueContext = parameterAssignmentContext.staticOrDynamicValue();
            map.put(attrName, Parameter.create(staticOrDynamicValueContext));
        }
        return map;
    }
}
